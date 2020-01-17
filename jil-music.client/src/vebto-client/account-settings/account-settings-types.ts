export interface ErrorsModel {
    password: {
        current_password?: string,
        new_password?: string,
        new_password_confirmation?: string,
    },
    account: {
        first_name?: string,
        last_name?: string,
        country?: string,
        language?: string,
        timezone?: string,
        phone?: string,
        company?: string,
        sms_code?: string,
    }
}

export interface PasswordModel {
    current_password?: string,
    new_password?: string,
    new_password_confirmation?: string
}