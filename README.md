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

### 1. OAuth Authentication

#### 1.1. Create the authenticator

```java
OAuthAuthenticator authenticator = new OAuthAuthenticator(
        // options
        OAuthOptions.DEFAULT
);
```

#### 1.2. Initial login

```java
authenticator.performAuthentication((record, profile) -> {
        // ...
});

// or...

AuthenticationRecord record = authenticator.performAuthentication();
```

#### 1.3. Refreshed login

```java
authenticator.performAuthentication(record, (newRecord, newProfile) -> {
        // ...
});

// or...

AuthenticationRecord record2 = authenticator.performAuthentication(
        record
);
```

### 1. Cookie Authentication

#### 1.1. Create the authenticator

```java
CookieAuthenticator authenticator = new CookieAuthenticator();
```

#### 1.2. Initial login

```java
authenticator.performAuthentication("your_microsoft_cookie_contents_here", (record, profile) -> {
        // ...
});
```

You may not refresh cookie based authentication. You must provide the cookie each time.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
