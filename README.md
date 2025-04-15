# Pandora Homedrive API
![Static Badge](https://img.shields.io/badge/scala-white?logo=scala&logoColor=DC322F&logoSize=auto&labelColor=white&color=%23DC322F&cacheSeconds=3600)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweight, functional local network storage service built with:
- **HTTP4S** (Pure functional HTTP)
- **Cats Effect** (Resource-safe IO)

## Feature List & Comparison
<div style="display: flex; gap: 20px;">
  <div style="flex: 1;">
    <table style="border-collapse: collapse; width: 100%;">
      <thead>
        <tr>
          <th colspan="2" style="background-color: #f2f2f2;">Pandora HomeDrive Features</th>
        </tr>
      </thead>
      <tbody>
        <tr><td>File upload/download</td><td>✅</td></tr>
        <tr><td>Folder organization</td><td>✅</td></tr>
        <tr><td>Local network storage</td><td>✅</td></tr>
        <tr><td>"Know Your Files" trivia game</td><td>✅</td></tr>
        <tr><td>Data encryption</td><td>✅</td></tr>
        <tr><td>Local data storage</td><td>✅</td></tr>
        <tr><td>Storage visualization</td><td>✅</td></tr>
        <tr><td>File type statistics</td><td>✅</td></tr>
      </tbody>
    </table>
  </div>

  <div style="flex: 1.5;">
    <table style="border-collapse: collapse; width: 100%;">
      <thead>
        <tr>
          <th>Feature</th>
          <th>Pandora HomeDrive</th>
          <th>Google Drive</th>
          <th>Dropbox</th>
          <th>OneDrive</th>
        </tr>
      </thead>
      <tbody>
        <tr><td>File upload/download</td><td>✅</td><td>✅</td><td>✅</td><td>✅</td></tr>
        <tr><td>Folder organization</td><td>✅</td><td>✅</td><td>✅</td><td>✅</td></tr>
        <tr><td>Local network sync</td><td>✅</td><td>❌</td><td>❌</td><td>❌</td></tr>
        <tr><td>Built-in file quiz game</td><td>✅</td><td>❌</td><td>❌</td><td>❌</td></tr>
        <tr><td>End-to-end encryption</td><td>✅</td><td>❌*</td><td>❌*</td><td>❌*</td></tr>
        <tr><td>Local-only storage option</td><td>✅</td><td>❌</td><td>❌</td><td>❌</td></tr>
        <tr><td>Storage analytics</td><td>✅</td><td>✅</td><td>❌</td><td>✅</td></tr>
        <tr><td>Free basic storage</td><td>✅</td><td>✅</td><td>✅</td><td>✅</td></tr>
        <tr><td>Real-time collaboration</td><td>❌</td><td>✅</td><td>✅</td><td>✅</td></tr>
        <tr><td>Mobile app support</td><td>❌</td><td>✅</td><td>✅</td><td>✅</td></tr>
      </tbody>
    </table>
  </div>
</div>

## Quick Start
```bash
git clone https://github.com/your-org/pandora-homedrive.git
cd pandora-homedrive
sbt run

# Server starts on http://localhost:8080
```
---

## Development

### Software/tools used

- IDE : IntelliJ (or any JVM friendly environment)
- Language : Scala 3.3.5
- Version-control: git/GitHub
- Build : sbt


## System Architecture

We will be using a **Microservice** architecture for our app. This means each team member will work on a separate, small server. This structure is chosen for its ease of work delegation and isolation.

- Microservices will communicate with each other via RESTful APIs
- Users will interact with the system through an **API Gateway**, which routes requests to the appropriate microservices

## Version Control (Git)

### Commit Messages
- We will use **conventional commit messages** (mandatory)
- Reference markdown files in the repo for formatting guidelines

### Code Conventions
- To be added in the repo README later

## GitHub Workflow

### GitHub Actions
- Automatic build tests run after pushing or merging a pull request
    - ✅ Green checkmark = passed
    - ❌ Red cross = failed
- **No pull requests will be accepted** into `main` or `development` branches unless they pass the build test

### Contribution Guidelines

#### Branching Strategy
1. Each microservice has its own branch
2. Create feature branches named: `[microservice]_service-{feat}`  
   Example: `chatbot_service-respond`
3. Work on features in their respective branches
4. When feature is complete:
    - Push changes to remote feature branch
    - Initiate pull request from feature branch → microservice branch
    - Resolve any conflicts
    - Ensure features pass build test (mandatory for microservice → development)
    - Merge with conventional commit message
    - Delete the feature branch after successful merge

#### Example Branch Structure During Development:
- Main

- Development

- api_gateway

- storage_service

- storage_service-upload

- storage_service-download

- chatbot_service

- analytics_service

## Team Responsibilities

| **Team Member**                                 | **Primary Responsibilities**                  |
|-------------------------------------------------|--------------------------------------------|
| [omardoescode](https://github.com/omardoescode) | Storage microservices                     |
| [moha09-coder](https://github.com/moha09-coder) | Middleware and security services          |
| [DEVUCP](https://github.com/DEVUCP)             | Microservice APIs, API gateway, frontend  |
| [marwanm-dev](https://github.com/marwanm-dev)                                 | Analytics microservice, assists Chatbot   |
| Abdelrahman Amr                                 | Chatbot microservice                      |

**Note:** All members will need to contribute to backend (Scala) development as it's mandatory for the project.