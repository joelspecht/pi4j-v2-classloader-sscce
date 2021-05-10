Pi4J :: SSCCE Showing Custom ClassLoader Issue
==========================================================================

This [Short, Self Contained, Correct (Compilable), Example](http://sscce.org/) application demonstrates PI4J v2's current inflexibility (as of 5/10/2021) to load plugin jars from a custom ClassLoader during runtime.

## How to Build

Maven 3.8 and JDK 11 are required to build this SSCCE.

From the project's root directory, run the following from the command line:

`mvn clean package`

## How to Run

Maven 3.8 and JRE 11 are required to run this SSCCE.

After building, from the project's root directory, run the following from the command line:

`mvn java:exec`

You should notice similar log messages to the following in your console output:

```
[com.joelspecht.pi4j.example.Main.main()] INFO com.pi4j.Pi4J - New auto context
[com.joelspecht.pi4j.example.Main.main()] INFO com.pi4j.Pi4J - New context builder
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-pwm; name=Mock PWM Provider]; com.pi4j.plugin.mock.provider.pwm.MockPwmProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-spi; name=Mock SPI Provider]; com.pi4j.plugin.mock.provider.spi.MockSpiProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-digital-output; name=Mock Digital Output (GPIO) Provider]; com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-analog-output; name=Mock Analog Output (GPIO) Provider]; com.pi4j.plugin.mock.provider.gpio.analog.MockAnalogOutputProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-serial; name=Mock Serial Provider]; com.pi4j.plugin.mock.provider.serial.MockSerialProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-analog-input; name=Mock Analog Input (GPIO) Provider]; com.pi4j.plugin.mock.provider.gpio.analog.MockAnalogInputProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-digital-input; name=Mock Digital Input (GPIO) Provider]; com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.provider.impl.DefaultRuntimeProviders - unable to 'initialize()' provider: [id=mock-i2c; name=Mock I2C Provider]; com.pi4j.plugin.mock.provider.i2c.MockI2CProvider referenced from a method is not visible from class loader
[com.joelspecht.pi4j.example.Main.main()] INFO com.pi4j.platform.impl.DefaultRuntimePlatforms - adding platform to managed platform map [id=mock-platform; name=Mock Platform; priority=-1000; class=com.pi4j.plugin.mock.platform.MockPlatform]
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.platform.impl.DefaultRuntimePlatforms - unable to 'initialize()' platform: [id=mock-platform; name=Mock Platform]; Pi4J provider [mock-analog-input] could not be found.  Please include this 'provider' JAR in the classpath.
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.platform.impl.DefaultRuntimePlatforms - Pi4J provider [mock-analog-input] could not be found.  Please include this 'provider' JAR in the classpath.
com.pi4j.exception.InitializeException: Pi4J provider [mock-analog-input] could not be found.  Please include this 'provider' JAR in the classpath.
        at com.pi4j.platform.PlatformBase.initialize(PlatformBase.java:265)
        at com.pi4j.platform.PlatformBase.initialize(PlatformBase.java:59)
        at com.pi4j.platform.impl.DefaultRuntimePlatforms.initializePlatform(DefaultRuntimePlatforms.java:295)
        at com.pi4j.platform.impl.DefaultRuntimePlatforms.add(DefaultRuntimePlatforms.java:238)
        at com.pi4j.platform.impl.DefaultRuntimePlatforms.add(DefaultRuntimePlatforms.java:213)
        at com.pi4j.platform.impl.DefaultRuntimePlatforms.initialize(DefaultRuntimePlatforms.java:190)
        at com.pi4j.runtime.impl.DefaultRuntime.initialize(DefaultRuntime.java:263)
        at com.pi4j.context.impl.DefaultContext.<init>(DefaultContext.java:103)
        at com.pi4j.context.impl.DefaultContext.newInstance(DefaultContext.java:72)
        at com.pi4j.context.impl.DefaultContextBuilder.build(DefaultContextBuilder.java:277)
        at com.pi4j.context.impl.DefaultContextBuilder.build(DefaultContextBuilder.java:48)
        at com.pi4j.Pi4J.newAutoContext(Pi4J.java:71)
        at com.joelspecht.pi4j.example.Main.<init>(Main.java:47)
        at com.joelspecht.pi4j.example.Main.main(Main.java:67)
        at org.codehaus.mojo.exec.ExecJavaMojo$1.run(ExecJavaMojo.java:254)
        at java.base/java.lang.Thread.run(Thread.java:834)
[com.joelspecht.pi4j.example.Main.main()] ERROR com.pi4j.platform.impl.DefaultRuntimePlatforms - unable to 'initialize()' platform: [id=mock-platform; name=Mock Platform]; Pi4J platform [mock-platform] failed to initialize(); Pi4J provider [mock-analog-input] could not be found.  Please include this 'provider' JAR in the classpath.
```

## What's the Problem?

I'd like to compile my code against `pi4j-core` but allow the user to provide plugin jars (such as `pi4j-plugin-mock`) in a well-known directory at runtime. In order to do this, I need to use a custom `ClassLoader` to look for plugins in the aforementioned plugins directory. This is typically done by setting the current Thread's context ClassLoader, but current PI4J v2 code is not respecting this ClassLoader in `com.pi4j.provider.impl.DefaultRuntimeProviders#add(Collection<T> provider)` (around line 267). When creating a provider proxy, it is instead using the ClassLoader which loaded the `com.pi4j.provider.Provider` class, which will be the Application's ClassLoader instance (which will not know how to find plugins in our special directory).

The simple fix is to change this provider proxy creation code from:

```java
var providerProxy = Proxy.newProxyInstance(
        Provider.class.getClassLoader(),
        ReflectionUtil.getAllInterfaces(providerInstance).toArray(new Class[]{}),
        handler);
```

to:

```java
var providerProxy = Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(),
        ReflectionUtil.getAllInterfaces(providerInstance).toArray(new Class[]{}),
        handler);
```
