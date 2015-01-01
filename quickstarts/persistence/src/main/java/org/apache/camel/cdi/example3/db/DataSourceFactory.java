package org.apache.camel.cdi.example3.db;

import org.h2.jdbcx.JdbcDataSource;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.sql.DataSource;

public class DataSourceFactory {

    @Produces
    @ApplicationScoped
    @DataSourceDefinition
    @Named("myDataSource")
    public DataSource initDataSource(InjectionPoint injectionPoint) {

        DataSourceDefinition dsdef = injectionPoint.getAnnotated().getAnnotation(DataSourceDefinition.class);

        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl(dsdef.url());
        ds.setUser(dsdef.user());
        ds.setPassword(dsdef.password());

        return (DataSource)ds;
    }
}
