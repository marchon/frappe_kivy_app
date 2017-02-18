package io.frappe.frappe_authenticator.authentication;

public class AccountGeneral {

    /**
     * Account type id
     */
    public static final String ACCOUNT_TYPE = "io.frappe.frappe_authenticator";

    /**
     * Account name
     */
    public static final String ACCOUNT_NAME = "Frappe";

    /**
     * Auth token types
     */
    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Frappe Server account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Frappe Server account";

    public static final ServerAuthenticate sServerAuthenticate = new FrappeServerAuthenticate();
}
