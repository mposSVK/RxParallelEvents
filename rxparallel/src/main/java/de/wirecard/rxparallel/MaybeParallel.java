package de.wirecard.rxparallel;

import com.jakewharton.rxrelay2.Relay;

import de.wirecard.rxparallel.util.SafeDisposeAction;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;

public class MaybeParallel<MAYBE, PARALLEL> extends Maybe<MAYBE> {

    private Maybe<MAYBE> mainMaybe;
    private Relay<PARALLEL> parallelRelay;
    private CompositeDisposable relayDisposable;

    private MaybeParallel(Maybe<MAYBE> mainMaybe, Relay<PARALLEL> parallelRelay) {
        if (mainMaybe == null)
            throw new NullPointerException("Main maybe can not be null");
        relayDisposable = new CompositeDisposable();
        this.mainMaybe = mainMaybe.doAfterTerminate(SafeDisposeAction.createAction(relayDisposable));
        this.parallelRelay = parallelRelay;
    }

    @Override
    protected void subscribeActual(MaybeObserver<? super MAYBE> observer) {
        mainMaybe.subscribe(observer);
    }

    public Maybe<MAYBE> subscribeParallel(Observer<PARALLEL> parallelObserver) {
        if (this.parallelRelay != null && parallelObserver != null) {
            this.parallelRelay.subscribeWith(parallelObserver);
        }
        return mainMaybe;
    }

    public Maybe<MAYBE> subscribeParallel(Relay<PARALLEL> parallelRelay) {
        if (this.parallelRelay != null && parallelRelay != null) {
            relayDisposable.add(this.parallelRelay.subscribe(parallelRelay));
        }
        return mainMaybe;
    }

    public static <MAYBE, PARALLEL> Function<? super Maybe<MAYBE>, MaybeParallel<MAYBE, PARALLEL>> with(final Relay<PARALLEL> parallelRelay) {
        return new Function<Maybe<MAYBE>, MaybeParallel<MAYBE, PARALLEL>>() {
            @Override
            public MaybeParallel<MAYBE, PARALLEL> apply(@NonNull Maybe<MAYBE> maybe) throws Exception {
                return new MaybeParallel<MAYBE, PARALLEL>(maybe, parallelRelay);
            }
        };
    }
}
