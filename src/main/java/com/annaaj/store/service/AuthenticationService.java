package com.annaaj.store.service;


import com.annaaj.store.exceptions.AuthenticationFailException;
import com.annaaj.store.repository.TokenRepository;
import com.annaaj.store.config.MessageStrings;
import com.annaaj.store.model.AuthenticationToken;
import com.annaaj.store.model.User;
import com.annaaj.store.utils.Helper;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    TokenRepository repository;

    public void saveConfirmationToken(AuthenticationToken authenticationToken) {
        repository.save(authenticationToken);
    }

    public AuthenticationToken getToken(User user) {
        return repository.findTokenByUser(user);
    }

    public User getUser(String token) {
        AuthenticationToken authenticationToken = repository.findTokenByToken(token);
        if (Helper.notNull(authenticationToken)) {
            if (Helper.notNull(authenticationToken.getUser())) {
                return authenticationToken.getUser();
            }
        }
        return null;
    }

    public void authenticate(String token) throws AuthenticationFailException {
        if (!Helper.notNull(token)) {
            throw new AuthenticationFailException(MessageStrings.AUTH_TOEKN_NOT_PRESENT);
        }
        if (!Helper.notNull(getUser(token))) {
            throw new AuthenticationFailException(MessageStrings.AUTH_TOEKN_NOT_VALID);
        }
    }

    public void removeAuthenticationToken(String token) {
        AuthenticationToken authenticationToken = repository.findTokenByToken(token);
        repository.delete(authenticationToken);
    }
}
