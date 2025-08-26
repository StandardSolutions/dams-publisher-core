package ru.standardsolutions.dams.publisher.common;

import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.sql.Connection;

public interface Database {

    AdvisoryLock newLock(Connection c, DamsOptions options);

    ChangeLog changelog();
}