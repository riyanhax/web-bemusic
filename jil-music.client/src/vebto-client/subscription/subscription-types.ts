export interface ErrorsModel {    
    account: {
        first_name?: string,
        last_name?: string,
        country?: string,
        language?: string,
        timezone?: string,
        phone?: string,
        company?: string,
        sms_code?: string,
    },
    subsribe: {
        voucher?: string,
    }  
}