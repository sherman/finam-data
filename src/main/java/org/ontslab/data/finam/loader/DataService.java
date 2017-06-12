package org.ontslab.data.finam.loader;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.NotNull;
import org.ontslab.data.finam.domain.SymbolSpec;

import java.io.File;

/**
 * @author Denis Gabaydulin
 * @since 11.06.17
 */
@ImplementedBy(DataServiceImpl.class)
public interface DataService extends Service {
    void load(@NotNull SymbolSpec symbolSpec, @NotNull File dataFile);
}
