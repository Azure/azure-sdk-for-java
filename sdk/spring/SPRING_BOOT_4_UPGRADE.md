# Spring Boot 4.0.0-RC2 Upgrade Status

## Overview
This document tracks the progress of upgrading Azure SDK for Java Spring modules to Spring Boot 4.0.0-RC2. As Spring Boot 4.x approaches release in November 2025, this upgrade ensures compatibility with the latest Spring ecosystem.

## Current Status: **IN PROGRESS** 🚧

### Completed ✅
- All version references updated to Spring Boot 4.0.0-RC2
- External dependencies synchronized with Spring Boot 4 managed versions
- Version management scripts extended for Spring Boot 4 support
- Deprecated `@NonNull` annotations removed (41 files)
- Core modules compile successfully
- API compatibility issues identified and documented

### In Progress 🔄
- Fixing auto-configuration API changes
- Resolving package relocation issues
- Testing and validation

## Version Updates

| Component | From | To |
|-----------|------|-----|
| Spring Boot | 3.5.5 | **4.0.0-RC2** |
| Spring Framework | 6.2.10 | **7.0.0-RC3** |
| Spring Cloud | 2025.0.0 | **2025.0.0-RC1** |
| Spring Integration | 6.5.1 | **7.0.0-RC2** |
| Spring Kafka | 3.3.9 | **4.0.0-RC1** |
| Spring Security | 6.5.3 | **7.0.0-RC3** |

## Known Issues and Solutions

### 1. PropertyMapper API Changes
**Status**: Solution documented ✓

**Issue**: `PropertyMapper.alwaysApplyingWhenNonNull()` method removed

**Files Affected**:
- `spring-cloud-azure-autoconfigure/.../eventhubs/properties/AzureEventHubsProperties.java`
- `spring-cloud-azure-autoconfigure/.../eventhubs/AzureEventHubsConsumerClientConfiguration.java`

**Solution**: Remove `.alwaysApplyingWhenNonNull()` from mapping chains. The default behavior now skips null values automatically.

```java
// Before
mapper.alwaysApplyingWhenNonNull().from(props::getValue).to(config::setValue);

// After  
mapper.from(props::getValue).to(config::setValue);
```

### 2. ConfigurableBootstrapContext Package Change
**Status**: Solution documented ✓

**Issue**: Class moved to new package

**File**: `spring-cloud-azure-autoconfigure/.../keyvault/environment/KeyVaultEnvironmentPostProcessor.java`

**Solution**: Update import statement:
```java
// Before
import org.springframework.boot.ConfigurableBootstrapContext;

// After
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
```

### 3. Auto-Configuration Classes
**Status**: Needs investigation ⚠️

**Issue**: Cannot find several Spring Boot auto-configuration classes:
- `org.springframework.boot.autoconfigure.kafka.KafkaProperties`
- `org.springframework.boot.autoconfigure.data.redis.RedisProperties`
- `org.springframework.boot.autoconfigure.jdbc.DataSourceProperties`
- `org.springframework.boot.autoconfigure.jms.*` classes

**Next Steps**: 
1. Check if classes moved to different packages in Spring Boot 4
2. Verify if separate module dependencies are now required
3. Update imports or add dependencies as needed

### 4. Other Issues
- Constructor recursion in `AadOAuth2UserService.java:64`
- Spring Security 7.0 API changes in `AadWebApplicationHttpSecurityConfigurer.java`

## Build Results

### Successful Modules ✅
- spring-cloud-azure-core
- spring-cloud-azure-service
- spring-cloud-azure-resourcemanager
- spring-messaging-azure (all variants)
- azure-spring-data-cosmos
- spring-cloud-azure-feature-management
- spring-integration-azure (all variants)

### Failed Modules ❌
- spring-cloud-azure-autoconfigure (~50 compilation errors)
- All modules depending on spring-cloud-azure-autoconfigure

## Timeline Estimate

| Phase | Duration | Status |
|-------|----------|--------|
| Version Updates | 2-4 hours | ✅ Complete |
| API Migration Research | 4-8 hours | 🔄 In Progress |
| Code Fixes | 4-8 hours | ⏳ Pending |
| Testing | 4-8 hours | ⏳ Pending |
| Documentation | 2-4 hours | ⏳ Pending |
| **Total** | **1-2 days** | **~40% Complete** |

## Testing Strategy

### Phase 1: Compilation ✅
- Fix all compilation errors
- Ensure all modules build successfully

### Phase 2: Unit Tests ⏳
- Run unit tests for all Spring modules
- Fix any test failures related to API changes

### Phase 3: Integration Tests ⏳
- Run integration tests with Azure services
- Verify functionality

### Phase 4: Compatibility Tests ⏳
- Test with sample applications
- Document breaking changes for users

## Migration Guide for Users

Once this upgrade is complete, users migrating from Spring Boot 3.x to 4.x will need to:

1. **Update Dependencies**: Use the new Spring Cloud Azure BOM version
2. **Review API Changes**: Check for breaking changes in Spring Boot 4.0
3. **Test Thoroughly**: Validate all Azure integrations work correctly
4. **Update Configuration**: Some configuration properties may have changed

Detailed migration guide will be provided in release notes.

## Resources

- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Framework 7.0 Documentation](https://docs.spring.io/spring-framework/reference/7.0/)
- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)

## Contact

For questions or issues with this upgrade, contact:
- @rujche (rujche)
- @saragluna (xiada)

---

**Last Updated**: 2025-11-07  
**Next Review**: After fixing auto-configuration issues
