package ru.standardsolutions.dams.publisher.common;

import ru.standardsolutions.dams.publisher.common.database.DbType;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.sql.Connection;

public interface Database {

    DbType type();

    String version();

    Connection connection();

    AdvisoryLock advisoryLock(Connection c, DamsOptions options);

    ChangeLog changelog();
}