package hu.ait.android.aloke.aloah.event;

import java.security.KeyPair;

/**
 * Created by Aloke on 5/15/15.
 */
public class CreateRSAKeysEvent {
    public final KeyPair pair;

    public CreateRSAKeysEvent(KeyPair pair) {
        this.pair = pair;
    }
}
