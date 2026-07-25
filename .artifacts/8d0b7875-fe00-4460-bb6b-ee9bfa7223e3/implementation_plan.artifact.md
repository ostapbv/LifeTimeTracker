# Repository Cleanup and Security Hardening

This plan outlines the steps to remove unnecessary and potentially personal IDE files from your public GitHub repository, ensuring your project remains clean and professional.

## User Review Required

> [!IMPORTANT]
> I found that your phone's unique **Serial Number** is currently visible in your GitHub history (inside `.idea/deploymentTargetSelector.xml`). While this is not a major security threat, it is personal data that should be removed.

## Proposed Changes

### [VCS Configuration]

#### [MODIFY] [.gitignore](file:///C:/Users/Ost/AndroidStudioProjects/LifeTimeTracker/.gitignore)
I will update the `.gitignore` to exclude personal IDE state files while keeping the configuration necessary for others to build the project.

#### [DELETE] [IDE State Files](file:///C:/Users/Ost/AndroidStudioProjects/LifeTimeTracker/.idea/)
I will stop tracking the following files in Git (they will remain on your computer but will be deleted from GitHub):
- `.idea/deploymentTargetSelector.xml` (Contains device serial number)
- `.idea/markdown.xml` (Unnecessary IDE state)
- `.idea/.name` (Unnecessary)

## Verification Plan

### Automated Verification
- I will run `git ls-files` to confirm that the sensitive files are no longer being tracked by Git.
- I will run `git status` to ensure the project remains clean.

### Manual Verification
- You can refresh your GitHub page after the push to verify that the `.idea/deploymentTargetSelector.xml` file has disappeared.
