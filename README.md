## Example of Gemfire using Reactive Spring Session on Kubernetes

This is a small PoC demonstrating how Gemfire can be used with reactive Spring Session. 
Gemfire doesn't have reactor support so the class `GemfireSessionRepository` implements some wrappers
around the blocking Gemfire calls. 

`GemFireHttpSessionConfiguration` and `AbstractGemFireHttpSessionConfiguration` had to be copied from OSS to remove
a hard coded dependency on `javax.servlet`

`GeodeLocatorEnvironmentPostProcessor` allows a cluster to be formed by looking up the peer pods within a statefulset.
Gemfire is running in embedded mode which is enabled using the annotations;

```java
@CacheServerApplication(name = "SpringSessionDataGeodeBootSampleServer", logLevel = "error") // <2>
@EnableManager(bindAddress = "0.0.0.0")
@EnableLocator(host = "0.0.0.0")
```

The sample app can be run on Kubernetes using `skaffold run`

To test the cache, port forward the service on port 8080, there is a web form which will save the cached session information