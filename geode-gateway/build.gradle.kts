plugins {
    java
    id("org.springframework.boot") version "2.6.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
//    id("com.google.cloud.tools.jib") version "3.1.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11
extra["springCloudVersion"] = "2021.0.0-RC1"
extra["springGeodeVersion"] = "1.6.0-RC1"
extra["springSessionVersion"] = "2021.1.0"
extra["kubernetesJavaClientVersion"] = "13.0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

sourceSets {
    main {
        output.setResourcesDir("build/classes/java/main")
    }
}
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("io.kubernetes:client-java:${property("kubernetesJavaClientVersion")}")
    implementation("io.kubernetes:client-java-spring-integration:${property("kubernetesJavaClientVersion")}")
    implementation("org.springframework.geode:spring-geode-starter")
    implementation("org.springframework.session:spring-session-core")
    implementation("org.springframework.session:spring-session-data-geode")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.geode:spring-geode-bom:${property("springGeodeVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.session:spring-session-bom:${property("springSessionVersion")}")
    }
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}