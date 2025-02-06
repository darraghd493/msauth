# msauth

A simple library for authenticating with Microsoft for Minecraft.

## Installation

### Gradle

```kotlin
repositories {
    maven {
        name = "darraghsRepositoryReleases"
        url = uri("https://repo.darragh.website/releases")
    }
}

dependencies {
    implementation("me.darragh:msauth:{version}")
}
```

_This project is also available via. Jitpack. View more information [here](https://jitpack.io/#Fentanyl-Client/msauth)._

## Usage

### 1. Create the authenticator

```java
OAuthAuthenticator authenticator = new OAuthAuthenticator(
        // options
        OAuthOptions.DEFAULT
);
```

### 2. Initial login

```java
authenticator.performAuthentication((record, profile) -> {
        // ...
});

// or...

AuthenticationRecord record = authenticator.performAuthentication();
```

### 3. Refreshed login

```java
authenticator.performAuthentication(record, (newRecord, newProfile) -> {
        // ...
});

// or...

AuthenticationRecord record2 = authenticator.performAuthentication(
        record
);
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
