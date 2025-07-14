#!/usr/bin/env node

import { execSync } from "child_process";
import fs from "fs-extra";
import path from "path";
import os from "os";
import inquirer from "inquirer";
import chalk from "chalk";

class MinecraftTemplateHelper {
  constructor() {
    this.availableBranches = [];
    this.templateConfig = {};
  }

  // Check if git is installed
  checkGitInstalled() {
    try {
      execSync("git --version", { stdio: "ignore" });
      return true;
    } catch (error) {
      return false;
    }
  }

  // Get available branches from GitHub repo
  async getAvailableBranches(
    repoUrl = "https://github.com/Xyndra/CustomMultiloaderTemplate.git",
  ) {
    try {
      const output = execSync(`git ls-remote --heads ${repoUrl}`, {
        encoding: "utf8",
      });
      const branches = output
        .split("\n")
        .filter((line) => line.trim())
        .map((line) => line.split("\t")[1].replace("refs/heads/", ""));
      return branches;
    } catch (error) {
      console.error(chalk.red("Error fetching branches from repository"));
      process.exit(1);
    }
  }

  // Validate mod ID
  validateModId(input) {
    const regex = /^[a-zA-Z0-9_-]+$/;
    if (!regex.test(input)) {
      return "Mod ID must contain only letters, numbers, hyphens, and underscores";
    }
    return true;
  }

  // Validate maven name
  validateMavenName(input) {
    const regex = /^[a-zA-Z][a-zA-Z0-9]*\.[a-zA-Z][a-zA-Z0-9]*$/;
    if (!regex.test(input)) {
      return "Maven name must be in format like 'com.example' or 'de.xyndra' (exactly two parts)";
    }
    return true;
  }

  // Validate main class name
  validateMainClassName(input) {
    const regex = /^[A-Z][a-zA-Z0-9]*$/;
    if (!regex.test(input)) {
      return "Main class name must start with uppercase letter and contain only letters and numbers";
    }
    return true;
  }

  // Get user input through CLI
  async getUserInput() {
    console.log(chalk.cyan("🎮 Minecraft Mod Template Helper"));
    console.log(
      chalk.gray(
        "Fill in the following information to generate your mod template:\n",
      ),
    );

    // Get available branches
    this.availableBranches = await this.getAvailableBranches();

    const questions = [
      {
        type: "list",
        name: "branch",
        message: "Select the template branch:",
        choices: this.availableBranches,
        default: this.availableBranches.includes("main")
          ? "main"
          : this.availableBranches[0],
      },
      {
        type: "input",
        name: "modName",
        message: "Mod name:",
        validate: (input) =>
          input.trim().length > 0 ? true : "Mod name cannot be empty",
      },
      {
        type: "input",
        name: "modId",
        message: "Mod ID:",
        default: (answers) => {
          return answers.modName
            .toLowerCase()
            .replace(/\s+/g, "_")
            .replace(/[^a-zA-Z0-9_-]/g, "");
        },
        validate: this.validateModId,
      },
      {
        type: "input",
        name: "modVersion",
        message: "Mod version:",
        default: "1.0.0",
        validate: (input) =>
          input.trim().length > 0 ? true : "Mod version cannot be empty",
      },
      {
        type: "input",
        name: "mavenName",
        message: "Maven name:",
        default: "de.xyndra",
        validate: this.validateMavenName,
      },
      {
        type: "input",
        name: "mainClassName",
        message: "Main class name:",
        default: (answers) => {
          return answers.modName
            .replace(/[^a-zA-Z0-9]/g, "")
            .replace(/^[^a-zA-Z]+/, "")
            .replace(/^[a-z]/, (char) => char.toUpperCase());
        },
        validate: this.validateMainClassName,
      },
      {
        type: "input",
        name: "modAuthors",
        message: "Author(s) (separated by commas):",
        validate: (input) =>
          input.trim().length > 0 ? true : "Authors cannot be empty",
        filter: (input) =>
          input
            .split(",")
            .map((author) => author.trim())
            .join(", "),
      },
      {
        type: "editor",
        name: "modDescription",
        message: "Mod description (multiline):",
        validate: (input) =>
          input.trim().length > 0 ? true : "Description cannot be empty",
      },
      {
        type: "input",
        name: "modLicense",
        message: "Mod license:",
        default: "All Rights Reserved",
        validate: (input) =>
          input.trim().length > 0 ? true : "License cannot be empty",
      },
    ];

    const answers = await inquirer.prompt(questions);

    // Process description to handle newlines
    answers.modDescription = answers.modDescription.replace(/\n/g, "\\n");

    return answers;
  }

  // Process files and replace default values
  async processFileContent(filePath, config) {
    try {
      let content;
      try {
        content = fs.readFileSync(filePath, "utf8");
      } catch (error) {
        // Skip files that can't be read as UTF-8 (likely binary)
        console.log(
          chalk.gray(
            `  Skipped binary: ${path.relative(process.cwd(), filePath)}`,
          ),
        );
        return;
      }

      const originalContent = content;

      // Convert mod ID for package name (replace - with _)
      const packageModId = config.modId.replace(/-/g, "_");
      const [mavenFirst, mavenSecond] = config.mavenName.split(".");

      // Debug: Log what we're looking for
      console.log(
        chalk.gray(`   Processing: ${path.relative(process.cwd(), filePath)}`),
      );

      // Replace package references in imports and other contexts
      // Handle import statements and package declarations
      if (content.includes("de.xyndra.examplemod")) {
        content = content.replace(
          /import\s+([^;]+\s+)?de\.xyndra\.examplemod/g,
          `import $1${config.mavenName}.${packageModId}`,
        );
        content = content.replace(
          /package\s+de\.xyndra\.examplemod/g,
          `package ${config.mavenName}.${packageModId}`,
        );
        console.log(
          chalk.gray(`     → Replaced import and package statements`),
        );
      }

      // Replace full package references
      if (content.includes("de.xyndra.examplemod")) {
        content = content.replace(
          /de\.xyndra\.examplemod/g,
          `${config.mavenName}.${packageModId}`,
        );
        console.log(chalk.gray(`     → Replaced full package references`));
      }

      // Replace maven name references
      if (content.includes("de.xyndra")) {
        content = content.replace(/de\.xyndra/g, config.mavenName);
        console.log(chalk.gray(`     → Replaced maven name references`));
      }

      // Replace default main class name
      if (content.includes("ExampleMod")) {
        content = content.replace(/ExampleMod/g, config.mainClassName);
        console.log(chalk.gray(`     → Replaced main class name`));
      }

      // Replace examplemod in content (case variations)
      if (content.includes("examplemod")) {
        content = content.replace(/examplemod/g, packageModId);
        console.log(chalk.gray(`     → Replaced examplemod (lowercase)`));
      }
      if (content.includes("EXAMPLEMOD")) {
        content = content.replace(/EXAMPLEMOD/g, packageModId.toUpperCase());
        console.log(chalk.gray(`     → Replaced EXAMPLEMOD (uppercase)`));
      }

      if (content.includes("example_mod")) {
        content = content.replace(/example_mod/g, config.modId);
        console.log(chalk.gray(`     → Replaced example_mod`));
      }
      if (content.includes("example-mod")) {
        content = content.replace(/example-mod/g, config.modId);
        console.log(chalk.gray(`     → Replaced example-mod`));
      }
      if (content.includes("Example Mod")) {
        content = content.replace(/Example Mod/g, config.modName);
        console.log(chalk.gray(`     → Replaced Example Mod`));
      }

      // Replace template variables
      if (content.includes("modAuthors=")) {
        content = content.replace(
          /modAuthors=.*$/gm,
          `modAuthors=${config.modAuthors}`,
        );
        console.log(chalk.gray(`     → Replaced modAuthors`));
      }
      if (content.includes("modDescription=")) {
        content = content.replace(
          /modDescription=.*$/gm,
          `modDescription=${config.modDescription.replace(/\r/g, "").replace(/\n/g, "")}`,
        );
        console.log(chalk.gray(`     → Replaced modDescription`));
      }
      if (content.includes("modLicense=")) {
        content = content.replace(
          /modLicense=.*$/gm,
          `modLicense=${config.modLicense}`,
        );
        console.log(chalk.gray(`     → Replaced modLicense`));
      }

      // Only write if content actually changed
      if (content !== originalContent) {
        await fs.writeFile(filePath, content, "utf8");
        console.log(
          chalk.green(`✓ Updated: ${path.relative(process.cwd(), filePath)}`),
        );
      } else {
        console.log(
          chalk.gray(`  No changes: ${path.relative(process.cwd(), filePath)}`),
        );
      }
    } catch (error) {
      console.error(
        chalk.red(`✗ Error processing ${filePath}: ${error.message}`),
      );
    }
  }

  // Rename directories and files
  async renamePathsRecursively(dir, config) {
    const items = await fs.readdir(dir);

    for (const item of items) {
      const fullPath = path.join(dir, item);
      const stat = await fs.stat(fullPath);

      if (stat.isDirectory()) {
        // Recursively process subdirectories first
        await this.renamePathsRecursively(fullPath, config);

        // Check if directory name needs to be renamed
        let newName = item;

        // Convert mod ID for package name (replace - with _)
        const packageModId = config.modId.replace(/-/g, "_");

        // Replace package structure directories (case variations)
        newName = newName.replace(/examplemod/g, packageModId);
        newName = newName.replace(/ExampleMod/g, config.mainClassName);
        newName = newName.replace(/EXAMPLEMOD/g, packageModId.toUpperCase());
        newName = newName.replace(/example_mod/g, config.modId);
        newName = newName.replace(/example-mod/g, config.modId);

        // Handle maven name directory structure replacement
        // Replace "de" and "xyndra" directory names with new maven parts
        const [mavenFirst, mavenSecond] = config.mavenName.split(".");

        // Only replace if it's exactly "de" or "xyndra" directory name
        if (item === "de") {
          newName = mavenFirst;
        } else if (item === "xyndra") {
          newName = mavenSecond;
        }

        // Also handle cases where directory names contain these as substrings
        if (item.includes("de")) {
          newName = newName.replace(/\bde\b/g, mavenFirst);
        }
        if (item.includes("xyndra")) {
          newName = newName.replace(/\bxyndra\b/g, mavenSecond);
        }

        if (newName !== item) {
          const newPath = path.join(dir, newName);
          await fs.move(fullPath, newPath);
          console.log(chalk.blue(`📁 Renamed directory: ${item} → ${newName}`));
          if (item === "de" || item === "xyndra" || item === "examplemod") {
            console.log(chalk.gray(`   Package structure update`));
          }
        }
      } else {
        // Check if file name needs to be renamed
        let newName = item;

        // Convert mod ID for package name (replace - with _)
        const packageModId = config.modId.replace(/-/g, "_");

        // Replace in filenames (case variations)
        newName = newName.replace(/examplemod/g, packageModId);
        newName = newName.replace(/ExampleMod/g, config.mainClassName);
        newName = newName.replace(/EXAMPLEMOD/g, packageModId.toUpperCase());
        newName = newName.replace(/example_mod/g, config.modId);
        newName = newName.replace(/example-mod/g, config.modId);

        // Handle maven name in filenames
        const [mavenFirst, mavenSecond] = config.mavenName.split(".");
        newName = newName.replace(/\bde\b/g, mavenFirst);
        newName = newName.replace(/\bxyndra\b/g, mavenSecond);

        if (newName !== item) {
          const newPath = path.join(dir, newName);
          await fs.move(fullPath, newPath);
          console.log(chalk.blue(`📄 Renamed file: ${item} → ${newName}`));
          if (
            item.includes("de") ||
            item.includes("xyndra") ||
            item.includes("examplemod")
          ) {
            console.log(chalk.gray(`   Package/template file update`));
          }
        }
      }
    }
  }

  // Create maven package structure
  async createMavenStructure(baseDir, config) {
    const srcPath = path.join(baseDir, "src", "main", "java");

    // Convert mod ID for package name (replace - with _)
    const packageModId = config.modId.replace(/-/g, "_");
    const [mavenFirst, mavenSecond] = config.mavenName.split(".");
    const fullPackageName = `${config.mavenName}.${packageModId}`;
    const packagePath = fullPackageName.replace(/\./g, path.sep);
    const fullPackagePath = path.join(srcPath, packagePath);

    // Check if old structure exists
    const oldPackagePath = path.join(srcPath, "de", "xyndra", "examplemod");
    const oldExists = await fs.pathExists(oldPackagePath);

    // Create the new package structure
    await fs.ensureDir(fullPackagePath);

    // Find existing java files and move them to correct package structure
    const javaFiles = await this.findFiles(srcPath, "**/*.java");

    if (javaFiles.length > 0) {
      for (const javaFile of javaFiles) {
        const fileName = path.basename(javaFile);
        const newPath = path.join(fullPackagePath, fileName);

        // Don't move if it's already in the right place
        if (javaFile !== newPath) {
          await fs.move(javaFile, newPath);
          console.log(chalk.blue(`📦 Moved to package: ${fileName}`));
        }
      }
    }

    // Remove old empty directory structure if it exists
    if (oldExists) {
      try {
        await fs.remove(path.join(srcPath, "de"));
        console.log(
          chalk.blue(`🗑️  Removed old package structure: de.xyndra.examplemod`),
        );
        console.log(
          chalk.blue(
            `📦 Created new package structure: ${mavenFirst}.${mavenSecond}.${packageModId}`,
          ),
        );
      } catch (error) {
        // Directory might not be empty or already removed, that's okay
      }
    }
  }

  // Find files matching a pattern
  async findFiles(dir, pattern) {
    const files = [];

    try {
      const items = await fs.readdir(dir);

      for (const item of items) {
        const fullPath = path.join(dir, item);
        try {
          const stat = await fs.stat(fullPath);

          if (stat.isDirectory()) {
            // Skip common directories that shouldn't be processed
            if (
              item === ".git" ||
              item === "node_modules" ||
              item === ".idea" ||
              item === "target" ||
              item === "build"
            ) {
              continue;
            }
            const subFiles = await this.findFiles(fullPath, pattern);
            files.push(...subFiles);
          } else if (this.matchesPattern(fullPath, pattern)) {
            files.push(fullPath);
          }
        } catch (statError) {
          // Skip files that can't be stat'd (might be broken symlinks)
          console.log(chalk.gray(`  Skipped inaccessible: ${fullPath}`));
        }
      }
    } catch (error) {
      // Directory might not exist, that's okay
    }

    return files;
  }

  // Simple pattern matching
  matchesPattern(filePath, pattern) {
    // Skip common binary and cache files
    const fileName = path.basename(filePath);
    const ext = path.extname(filePath).toLowerCase();

    // Skip binary files and common non-text files
    const binaryExtensions = [
      ".jar",
      ".class",
      ".png",
      ".jpg",
      ".jpeg",
      ".gif",
      ".ico",
      ".zip",
      ".tar",
      ".gz",
      ".exe",
      ".dll",
      ".so",
      ".dylib",
    ];
    if (binaryExtensions.includes(ext)) {
      return false;
    }

    // Skip cache and build files
    if (
      fileName.startsWith(".") &&
      (fileName.endsWith(".cache") || fileName.endsWith(".tmp"))
    ) {
      return false;
    }

    // For **/* pattern, just return true for all files (we already filtered binaries above)
    if (pattern === "**/*") {
      return true;
    }

    const regex = pattern.replace(/\*\*/g, ".*").replace(/\*/g, "[^/\\\\]*");

    const finalRegex = new RegExp(regex + "$");
    const result = finalRegex.test(filePath);

    return result;
  }

  // Process all files in the template
  async processAllFiles(templateDir, config) {
    const files = await this.findFiles(templateDir, "**/*");

    console.log(chalk.blue(`📄 Found ${files.length} files to process`));

    for (const file of files) {
      try {
        const stat = await fs.stat(file);
        if (stat.isFile()) {
          await this.processFileContent(file, config);
        }
      } catch (error) {
        console.log(chalk.yellow(`⚠️  Skipped file ${file}: ${error.message}`));
      }
    }
  }

  // Clone repository and process template
  async cloneAndProcess(config, outputDir) {
    const tempDir = path.join(os.tmpdir(), `mc-template-${Date.now()}`);
    const repoUrl = "https://github.com/Xyndra/CustomMultiloaderTemplate.git";

    try {
      console.log(chalk.blue("📥 Cloning template repository..."));
      execSync(`git clone -b ${config.branch} ${repoUrl} ${tempDir}`, {
        stdio: "inherit",
      });

      console.log(chalk.blue("📁 Renaming directories and files..."));
      await this.renamePathsRecursively(tempDir, config);

      console.log(chalk.blue("📦 Creating maven package structure..."));
      await this.createMavenStructure(tempDir, config);

      console.log(chalk.blue("🔧 Processing template files..."));
      await this.processAllFiles(tempDir, config);

      console.log(chalk.blue("📁 Moving files to output directory..."));
      await fs.ensureDir(outputDir);
      await fs.copy(tempDir, outputDir);

      console.log(chalk.blue("🧹 Cleaning up..."));

      // Robust cleanup with retry logic for Windows
      let cleanupAttempts = 0;
      const maxCleanupAttempts = 3;

      while (cleanupAttempts < maxCleanupAttempts) {
        try {
          if (await fs.pathExists(tempDir)) {
            await fs.remove(tempDir);
            break;
          }
        } catch (cleanupError) {
          cleanupAttempts++;
          if (cleanupAttempts < maxCleanupAttempts) {
            console.log(
              chalk.yellow(
                `⚠️  Cleanup attempt ${cleanupAttempts} failed, retrying...`,
              ),
            );
            // Wait a bit before retrying
            await new Promise((resolve) => setTimeout(resolve, 1000));
          } else {
            console.log(
              chalk.yellow(`⚠️  Could not remove temp directory: ${tempDir}`),
            );
            console.log(
              chalk.gray(`    You may need to manually delete it later.`),
            );
          }
        }
      }

      console.log(chalk.green("✅ Template generated successfully!"));
      console.log(chalk.gray(`Output directory: ${outputDir}`));
      console.log(chalk.yellow("\n📋 Summary of changes made:"));
      console.log(
        chalk.gray(
          `  • Package: de.xyndra.examplemod → ${config.mavenName}.${config.modId.replace(/-/g, "_")}`,
        ),
      );
      console.log(
        chalk.gray(`  • Main class: ExampleMod → ${config.mainClassName}`),
      );
      console.log(chalk.gray(`  • Mod ID: examplemod → ${config.modId}`));
      console.log(
        chalk.gray(`  • Maven name: de.xyndra → ${config.mavenName}`),
      );
    } catch (error) {
      console.error(
        chalk.red("❌ Error during template processing:"),
        error.message,
      );

      // Cleanup on error with retry logic
      let cleanupAttempts = 0;
      const maxCleanupAttempts = 3;

      while (cleanupAttempts < maxCleanupAttempts) {
        try {
          if (await fs.pathExists(tempDir)) {
            await fs.remove(tempDir);
            break;
          }
        } catch (cleanupError) {
          cleanupAttempts++;
          if (cleanupAttempts < maxCleanupAttempts) {
            // Wait a bit before retrying
            await new Promise((resolve) => setTimeout(resolve, 1000));
          } else {
            console.log(
              chalk.yellow(`⚠️  Could not remove temp directory: ${tempDir}`),
            );
            console.log(
              chalk.gray(`    You may need to manually delete it later.`),
            );
          }
        }
      }
    }
  }

  // Main execution function
  async run() {
    try {
      // Check if git is installed
      if (!this.checkGitInstalled()) {
        console.error(
          chalk.red(
            "❌ Git is not installed or not in PATH. Please install Git and try again.",
          ),
        );
        process.exit(1);
      }

      console.log(chalk.green("✓ Git is installed"));

      console.log(
        chalk.red(
          'Note 1: This is "Vibe Coded", which means I will not take any responsibility what happens if you don\'t quit right now with Ctrl+C',
        ),
      );
      console.log(
        chalk.red(
          "Note 2: This is my template. I don't really care if you steal it, but I will get very angry if you " +
            "impersonate me by using the `de.xyndra` namespace, even if it is the default one.",
        ),
      );

      // Get user input
      const config = await this.getUserInput();

      // Display summary
      console.log(chalk.yellow("\n📋 Configuration Summary:"));
      console.log(chalk.gray(`Branch: ${config.branch}`));
      console.log(chalk.gray(`Mod Name: ${config.modName}`));
      console.log(chalk.gray(`Mod ID: ${config.modId}`));
      console.log(chalk.gray(`Version: ${config.modVersion}`));
      console.log(chalk.gray(`Maven Name: ${config.mavenName}`));
      console.log(chalk.gray(`Main Class: ${config.mainClassName}`));
      console.log(chalk.gray(`Authors: ${config.modAuthors}`));
      console.log(chalk.gray(`License: ${config.modLicense}`));
      console.log(
        chalk.gray(
          `Description: ${config.modDescription.replace(/\\n/g, "\n             ")}`,
        ),
      );

      // Confirm generation
      const { confirm } = await inquirer.prompt([
        {
          type: "confirm",
          name: "confirm",
          message: "Generate mod template with these settings?",
          default: true,
        },
      ]);

      if (!confirm) {
        console.log(chalk.yellow("Template generation cancelled."));
        return;
      }

      // Generate template
      const outputDir = path.join(process.cwd(), config.modId);
      await this.cloneAndProcess(config, outputDir);
    } catch (error) {
      console.error(chalk.red("❌ An error occurred:"), error.message);
      process.exit(1);
    }
  }
}

const helper = new MinecraftTemplateHelper();
helper.run();
