# Build To Order Management System (BTOMS)
![image](https://github.com/user-attachments/assets/179913b2-f8f2-4614-9b81-eca53e06dc98)

![UML Class Diagram](https://img.shields.io/badge/UML%20Class%20Diagram-1976D2?style=for-the-badge&logoColor=white)
![Solid Design Principles](https://img.shields.io/badge/SOLID%20Design%20Principles-C71A36?style=for-the-badge&logoColor=white)
![OOP Concepts](https://img.shields.io/badge/OOP%20Concepts-C71A36?style=for-the-badge&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white)

**Team:** [<img src="https://github.com/javiertay.png" height="20" width="20" /> Javier Tay](https://github.com/javiertay) |
[<img src="https://github.com/BaconPancakez.png" height="20" width="20" /> Seng Qi Ming](https://github.com/BaconPancakez) |
[<img src="https://github.com/Everwi8.png" height="20" width="20" /> Perrin-Owen Heng](https://github.com/Everwi8) |
[<img src="https://github.com/jeeezzz.png" height="20" width="20" /> Jessica Joyson](https://github.com/jeeezzz) |
[<img src="https://github.com/j9op123-d.png" height="20" width="20" /> Quan Hui Shan Audrey](https://github.com/j9op123-d)

**Docs:** [Report]() | 
[UML Class Diagram]() |
[Java Docs]()

A Java-based application to manage **Build-To-Order (BTO)** flat applications, featuring authentication, flat browsing and filtering, project application submission, receipt generation, and role-based operations for applicants, officers, and managers, while ensuring adherence to OOP ideas.
This README file provides instructions on how to clone, compile, and run the project.<br>

## Table of Contents

- [Setup Instructions](#setup-instructions)
    - [Using the Terminal](#using-the-terminal)
    - [Using an IDE (Eclipse/IntelliJ)](#using-an-ide-eclipseintellij)
- [Generating JavaDocs](#generating-javadocs)
  - [Using the Terminal](#using-the-terminal-1)
  - [Using an IDE](#using-an-ide)
- [Usage](#usage)
  - [Login Credentials](#login-credentials)

## Directory Layout

```
./
├── .github/
├── lib/                                # apache poi jar files
│   └── ...
├── src/                                # Application source
│   ├── controller/                     # controller files
│   │   └── ...
│   ├── data/                           # data files (Excel)
│   │   └── CombinedExcel.xlsx
│   ├── main/                           
│   │   ├── MainApp.java                # controller files
│   ├── model/                          # model files
│   │   └── ...
│   ├── util/                           # utility helper files
│   │   └── ...
│   ├── view/                           # cli files
│   │   └── ...
├── doc/
│   ├── index.html                      # Javadoc site
│   └── ...
├── report/
│   ├── SC2002 Report.pdf               # Report document
├── test/java/controller                # unit test files
│   └── ...
├── .gitignore
└── README.md
```

## Setup Instructions

### Using the Terminal

These setup instructions will guide you through the process of cloning the repository, navigating to the cloned repository, compiling the project, and running the project in your terminal.

1. Open your terminal

2. Clone the repository by entering the following command:
```bash
git clone https://github.com/javiertay/SC2002.git
```

3. Navigate to the cloned repository by entering the following command:
```
cd SC2002
```

4. Compile the project by entering the following command:
```bash
javac -cp src -d bin src/main/MainApp.java
```

5. Run the project by entering the following command:
```bash
java -cp bin main.MainApp
```

Congratulations, you have successfully cloned, compiled, and run the BTOMS project!

### Using an IDE

1. Import the project as a Maven/Java project via VCS > Git > Clone URI (if using IntelliJ) or File > Import > Git > Projects from Git (if using Eclipse).
2. Set the source folder (`src`) on the project build path.
3. Run `MainApp.java` as a Java Application.

## Generating JavaDocs

### Using the Terminal

Run the following command at the project root to generate Javadocs into the `docs` directory:

```bash
javadoc -d doc -sourcepath src -subpackages controller:main:model:util:view -classpath "lib/*" -author -version -private -noqualifier all
```
then to open and view
```
start doc/index.html
```
### Using an IDE

1. In Eclipse: Project > Generate Javadoc, select packages, enable "Private" members, choose output directory.
2. In IntelliJ: Tools > Generate JavaDoc, configure scope and output directory.

## Usage

After launching, follow on-screen prompts to log in, browse and filter flats, submit applications, and generate receipts. Role-specific menus guide Applicants, Officers, and Managers through their respective workflows.

### Login Credentials

This section contains some login credentials for users with different roles. The full list is available in `data/CombinedExcel.xlsx`. Example entries:

**Applicants:**
```
NRIC: S1234567A, Password: password
```

**Officers:**
```
NRIC: T2109876H, Password: password
```

**Managers:**
```
NRIC: S5678901G, Password: password
```
> **Note:** Update \`data/CombinedExcel.xlsx\` with your own sample data to continue testing the full scope.
