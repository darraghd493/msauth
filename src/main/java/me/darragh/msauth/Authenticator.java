package me.darragh.msauth;

import lombok.SneakyThrows;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The standard authenticator interface.
 *
 * @author darraghd493
 * @since 1.0.0
 *
 * @param <T> The type of authentication record used to store authentication data.
 */
public interface Authenticator<T extends AuthenticationRecord> {
    /**
     * Intended to re-authenticate the user using previously stored data.
     *
     * @param record The record to use for authentication.
     *               Depending on the implementation, this may be ignored/partially used.
     * @param callback The callback to call when the authentication is complete.
     */
    void performAuthentication(T record, AuthenticationCallback<T> callback);

    /**
     * Perform the initial authentication of the user.
     *
     * @param callback The callback to call when the authentication is complete.
     */
    void performAuthentication(AuthenticationCallback<T> callback);

    /**
     * Intended to re-authenticate the user using previously stored data.
     *
     * @implNote This method is blocking and should not be called on the main thread.
     *
     * @param record The record to use for authentication.
     *               Depending on the implementation, this may be ignored/partially used.
     * @return The authentication record.
     */
    @SneakyThrows
    default T performAuthentication(T record) {
        AtomicReference<T> ref = new AtomicReference<>(null);
        performAuthentication(record, (record1, profile) -> ref.set(record1));
        while (ref.get() == null) {
            //noinspection BusyWait
            Thread.sleep(100L);
        }
        return ref.get();
    }

    /**
     * Perform the initial authentication of the user.
     *
     * @implNote This method is blocking and should not be called on the main thread.
     *
     * @return The authentication record.
     */
    @SneakyThrows
    default T performAuthentication() {
        AtomicReference<T> ref = new AtomicReference<>(null);
        performAuthentication((record, profile) -> ref.set(record));
        while (ref.get() == null) {
            //noinspection BusyWait
            Thread.sleep(100L);
        }
        return ref.get();
    }

    /**
     * Forcefully stops the current authentication process.
     */
    void stopAuthentication();

    /**
     * Returns whether the authenticator is currently authenticating.
     *
     * @return Whether the authenticator is currently authenticating.
     */
    boolean isAuthenticating();
}
