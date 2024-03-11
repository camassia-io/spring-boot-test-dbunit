# Publishing

## Prerequisites

- GPG Installed (e.g. via homebrew or https://gnupg.org/download/index.html#sec-1-2)
- Environment Variables:
- - `GPG_SIGNING_KEY`: See Signing Step 4
- - `GPG_SIGNING_PASSPHRASE`: See Signing Step 1
- - `OSSRH_USERNAME`: Sonatype Nexus Username
- - `OSSRH_PASSWORD`: Sonatype Nexus Password

## Signing Keys

Maven Central requires all artifacts to be signed using GPG
See https://central.sonatype.org/publish/requirements/gpg/#generating-a-key-pair for more info

### 1) Create a key pair

`gpg --gen-key`

Note the above generates a key that lasts for 2 years

Set the following variable `GPG_SIGNING_PASSWORD={password used}`

### 2) Fetch the Public Key ID

`gpg --list-keys`

The key ID is the long uppercase String under `pub`

### 3) Distribute the Public Key to a keyserver

##### Via Command Line

Publish to one of the recommended key servers: keyserver.ubuntu.com, keys.openpgp.org, pgp.mit.edu

`gpg --keyserver keyserver.ubunto.com --send-keys {KEYID}`

##### Manually

if that fails, go to [keyserver site](https://keyserver.ubuntu.com) and upload manually using the following from clipboard:

`gpg --armor --output public-key.gpg --export your@email.com | pbcopy`

### 4) Set up the `GPG_SIGNING_KEY` Environment variable

`gpg --armor --export-secret-keys your@email.com | awk 'NR == 1 { print "GPG_SIGNING_KEY=" } 1' ORS='\\n' | pbcopy`

## Publishing

Ensure the `GITHUB_VERSION` environment variable is set as required

`./gradlew clean publishToSonatype closeAndReleaseStagingRepositories`

### If this fails

Go to https://oss.sonatype.org/#stagingRepositories 

Check if all the validation rules have passed under the "Activity" tab. Fix any failures & try to close via the UI.
