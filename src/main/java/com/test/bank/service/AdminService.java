package com.test.bank.service;

import com.test.bank.initializer.DataSourceInitializer;
import com.test.bank.model.AdminUser;
import com.test.bank.tool.PasswordUtils;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.types.UInteger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.SecureRandom;
import java.util.Base64;

import static com.test.bank.db.Tables.ADMIN;
import static com.test.bank.db.Tables.TOKEN;

// import record class for storing returned record from JOOQ queries
import com.test.bank.db.tables.records.TokenRecord;

@Singleton
public class AdminService {

    DefaultConfiguration jooqConfiguration;

    @Inject
    public AdminService(DataSourceInitializer dataSourceInitializer) {
        this.jooqConfiguration = dataSourceInitializer.getJooqConfiguration();
    }

    public String login(String account, String password) {
        String token = null;
        AdminUser adminUser = DSL.using(jooqConfiguration).fetchOne(ADMIN, ADMIN.ACCOUNT.eq(account)).into(AdminUser.class);
        if (PasswordUtils.verifyUserPassword(password, adminUser.getPassword(), adminUser.getSalt())) {
            token = generateToken();
            DSL.using(jooqConfiguration).insertInto(TOKEN, TOKEN.ADMINID, TOKEN.TOKEN_)
                    .values(UInteger.valueOf(adminUser.getId()), token)
                    .onDuplicateKeyUpdate()
                    .set(TOKEN.TOKEN_, token)
                    .execute();
        }
        return token;
    }

    // called before transaction (in TransactionResource)
    public boolean authenticate(String token) {
        // implement authenticate

        System.out.println("Searching for token: " + token);
        TokenRecord record = DSL.using(jooqConfiguration).fetchOne(TOKEN, TOKEN.TOKEN_.eq(token));

        if (record != null) {

            System.out.println("Token fetched: AdminId=" + record.getAdminid() + ", Token=" + record.getToken());

            return true;
        }
        
        System.out.println("Token not found");
        return false;
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

}
