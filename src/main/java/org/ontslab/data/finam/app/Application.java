package org.ontslab.data.finam.app;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.ontslab.data.finam.domain.Period;
import org.ontslab.data.finam.domain.SymbolSpec;
import org.ontslab.data.finam.loader.DataService;

import java.io.File;

/**
 * @author Denis Gabaydulin
 * @since 12.06.17
 */
public class Application {
    public static void main(String[] args) {
        Preconditions.checkArgument(args.length > 1, "Path to a date file is required");

        File dataFile = new File(args[1]);
        int elts = Integer.parseInt(args[2]);

        Injector injector = Guice.createInjector();

        // TODO: add configuration
        SymbolSpec symbolSpec = new SymbolSpec();
        symbolSpec.setName("SPFB.Si");
        symbolSpec.setPeriod(Period.FIVE_MINUTES);
        symbolSpec.setEm(19899);
        symbolSpec.setMarketId(14);
        symbolSpec.setDays(elts);

        DataService dataService = injector.getInstance(DataService.class);

        dataService.startAsync().awaitRunning();
        dataService.load(symbolSpec, dataFile);
        dataService.stopAsync().awaitTerminated();
    }
}
