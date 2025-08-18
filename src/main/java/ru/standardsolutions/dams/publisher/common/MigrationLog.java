package ru.standardsolutions.dams.publisher.common;

import java.util.List;

public interface MigrationLog {
    List<MigrationAction> actions();
}
