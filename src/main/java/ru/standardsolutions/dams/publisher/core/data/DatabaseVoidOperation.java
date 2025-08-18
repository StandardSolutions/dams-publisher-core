package ru.standardsolutions.dams.publisher.core.data;

import java.sql.SQLException;

public interface DatabaseVoidOperation {

    void execute() throws SQLException;
}