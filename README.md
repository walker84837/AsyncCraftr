# AsyncCraftr

AsyncCraftr abstracts the boilerplate of handling both Folia and non-Folia asynchronous code.

## Table of Contents

- [Usage](#usage)
- [API](#asynccraftr-api)
- [Roadmap](#roadmap)
- [License](#license)

## Usage

Include the `https://maven.winlogon.org/releases` Maven repository and import `org.winlogon:asynccraftr:0.1.0`.

## How it works

AsyncCraftr has an internal boolean which is [evaluated](https://docs.papermc.io/paper/dev/folia-support/#checking-for-folia) when the class is loaded, which is when your plugin loads. Based on this, it checks whether to use the [BukkitScheduler](https://jd.papermc.io/paper/1.21.6/org/bukkit/scheduler/BukkitScheduler.html) API, or Folia's [various schedulers](https://docs.papermc.io/paper/dev/folia-support/#schedulers) (global, region, async, entity) for each async/sync task.

### Documentation

You can generate the documentation using `./gradlew javadoc`. The HTML documentation files will be in `asynccraftr/build/docs/javadoc`. You can run an [HTTP server](https://github.com/walker84837/lichen), in that folder or open your browser.

## Roadmap

- [ ] Complete API documentation
- [ ] Use Codeberg Pages to host Javadoc

## License

This library is licensed under the [MIT](LICENSE) license.
