# Publishing

This project is published to Sonatype Nexus

To Create a new release, go to [Releases](https://github.com/camassia-io/spring-boot-test-dbunit/releases) & Draft a new release. 
This will trigger the [release workflow](https://github.com/camassia-io/spring-boot-test-dbunit/blob/main/.github/workflows/release.yml) which will build the project, sign the artefacts & publish to [Sonatype Nexus](https://oss.sonatype.org/#view-repositories;public~browsestorage~io/camassia)

## Publish to Maven Local

This just requires the GITHUB_VERSION

`GITHUB_VERSION=xxx-SNAPSHOT ./gradlew publishToMavenLocal`

## Publish to Nexus / Maven Central

### Prerequisites

- GPG Installed (e.g. via homebrew or https://gnupg.org/download/index.html#sec-1-2)
- Environment Variables:
  - `GPG_SIGNING_KEY`: See Signing Step 4
  - `GPG_SIGNING_PASSPHRASE`: See Signing Step 1
  - `OSSRH_USERNAME`: Sonatype Nexus Username
  - `OSSRH_PASSWORD`: Sonatype Nexus Password
  - `GITHUB_VERSION`: The version number, usually of a Github Release

### Signing Keys

Maven Central requires all artifacts to be signed using GPG
See https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair for more info

#### 1) Create a key pair

`gpg --gen-key`

Note the above generates a key that lasts for 2 years

Set the following variable `GPG_SIGNING_PASSWORD={password used}`

#### 2) Fetch the Public Key ID

`gpg --list-keys`

The key ID is the long uppercase String under `pub`

#### 3) Distribute the Public Key to a keyserver

##### Via Command Line

Publish to one of the recommended key servers: keyserver.ubuntu.com, keys.openpgp.org, pgp.mit.edu

`gpg --keyserver keyserver.ubunto.com --send-keys {KEYID}`

##### Manually

if that fails, go to [keyserver site](https://keyserver.ubuntu.com) and upload manually using the following from clipboard:

`gpg --armor --output public-key.gpg --export your@email.com | pbcopy`

#### 4) Set up the `GPG_SIGNING_KEY` Environment variable

The below copies the private key to your clipboard

`gpg --armor --export-secret-keys your@email.com | pbcopy`

### Publishing

Ensure the `GITHUB_VERSION` environment variable is set as required

`./gradlew clean publishToSonatype closeAndReleaseStagingRepositories`

#### If this fails

Go to https://oss.sonatype.org/#stagingRepositories 

Check if all the validation rules have passed under the "Activity" tab. Fix any failures & try to close via the UI.
