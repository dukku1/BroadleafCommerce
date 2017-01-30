/*
 * #%L
 * BroadleafCommerce Common Libraries
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.common.demo;

import org.broadleafcommerce.common.admin.condition.AdminExistsCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jeff Fischer
 */
@Configuration("blCommonData")
@Conditional(ImportCondition.class)
public class ImportSQLConfig {

    @Bean
    @Conditional(DemoCondition.class)
    public AutoImportSql blCommonBasicData() {
        return new AutoImportSql(AutoImportPersistenceUnit.BL_PU,"config/bc/sql/demo/load_i18n_countries.sql", AutoImportStage.PRIMARY_BASIC_DATA);
    }

    @Bean
    @Conditional({DemoCondition.class, AdminExistsCondition.class})
    public AutoImportSql blCommonAdminData() {
        return new AutoImportSql(AutoImportPersistenceUnit.BL_PU,"config/bc/sql/demo/load_admin_users.sql", AutoImportStage.PRIMARY_FRAMEWORK_SECURITY);
    }

    @Bean
    @Conditional({MTCondition.class, DemoCondition.class})
    public AutoImportSql blCommonLateData() {
        return new AutoImportSql(AutoImportPersistenceUnit.BL_PU,"config/bc/sql/demo/fix_system_property_data.sql", AutoImportStage.PRIMARY_LATE);
    }

    @Bean
    @Conditional({MTCondition.class, AdminExistsCondition.class, DemoCondition.class})
    public AutoImportSql blCommonLateAdminData() {
        return new AutoImportSql(AutoImportPersistenceUnit.BL_PU,"config/bc/sql/demo/fix_admin_user_data.sql", AutoImportStage.PRIMARY_LATE);
    }
}
