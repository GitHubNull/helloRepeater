# AGENTS.md - helloRepeater Coding Guidelines

## Project Overview
Burp Suite plugin for managing Repeater tabs with auto-rename and grouping.
- **Build System**: Maven
- **Java Version**: 17
- **Package**: `org.oxff.repeater`

## Build Commands

```bash
# Build the project
mvn clean compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Build JAR (with dependencies)
mvn clean package -DskipTests

# Build without tests
mvn clean package -DskipTests

# Clean build artifacts
mvn clean

# Install to local repository
mvn install
```

## Code Style Guidelines

### Imports
- Group imports: java.*, javax.*, third-party, project
- No wildcard imports (except for Swing constants)
- Import order: static imports first, then java, javax, org, com

### Formatting
- Indent: 4 spaces (no tabs)
- Max line length: 120 characters
- Opening brace on same line
- Blank line between class members
- Always use braces for if/for/while

### Naming Conventions
- Classes: `CamelCase` (e.g., `ContextMenuProvider`)
- Interfaces: `CamelCase` ending with type (e.g., `BurpExtension`)
- Methods: `camelCase` (e.g., `generateTitle`)
- Variables: `camelCase` (e.g., `sqliteStorage`)
- Constants: `UPPER_SNAKE_CASE`
- Private fields: prefix with `this.`
- Static fields: `ALL_CAPS`

### Types
- Use `List<>` instead of raw types
- Use generics properly with diamond operator `<>`
- Prefer interfaces over implementations (e.g., `List` over `ArrayList`)
- Use `final` for method parameters that won't be modified

### Error Handling
- Catch specific exceptions, not `Exception`
- Log errors via `api.logging().logToError()`
- Show user-friendly messages via `JOptionPane`
- Never swallow exceptions silently

### Burp Extension Patterns
- Main entry: Implement `BurpExtension`
- Use `MontoyaApi` for all Burp interactions
- Register tabs: `api.userInterface().registerSuiteTab()`
- Register context menus: `api.userInterface().registerContextMenuItemsProvider()`
- Store data: Use `api.persistence().extensionData()` for Burp storage, SQLite for rules

### Package Structure
```
org.oxff.repeater/
├── config/          # UI dialogs and tabs
├── menu/            # Context menu providers
├── model/           # Entity classes (Group, RenameRule)
├── rename/          # Repeater rename logic
└── storage/         # Data persistence layer
```

### Comments
- Class-level Javadoc for public classes
- Method-level Javadoc for public API methods
- Inline comments for complex logic only
- TODO comments for future improvements

### Best Practices
- Keep methods under 50 lines
- Keep classes focused and cohesive
- Use dependency injection via constructor
- Never expose internal mutable state
- Close resources in finally blocks or use try-with-resources

## Git Workflow
```bash
# Before commit
mvn clean compile

# Commit message format
type: brief description

# Types: feat, fix, refactor, docs, chore, test
```