import com.google.inject.Guice;
import com.google.inject.Injector;

import guice.AkkaModule;


public class Runner {

    public static void main(final String[] args){

        final Injector injector = Guice.createInjector(new AkkaModule());

    }
}
