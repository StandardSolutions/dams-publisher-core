package ru.standardsolutions.dams.publisher.core.data.migration;

import java.util.List;

public interface MigrationLog {
    List<MigrationAction> actions();
}
