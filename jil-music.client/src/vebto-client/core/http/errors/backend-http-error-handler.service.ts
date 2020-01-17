import {Injectable} from '@angular/core';
import {CurrentUser} from "vebto-client/auth/current-user";
import {Router} from "@angular/router";
import {Toast} from "../../ui/toast.service";
import {Translations} from "../../translations/translations.service";
import {HttpErrorHandler} from './http-error-handler.service';

declare let Android: any;

@Injectable()
export class BackendHttpErrorHandler extends HttpErrorHandler {

    /**
     * HttpErrorHandler Constructor.
     */
    constructor(
        protected i18n: Translations,
        protected currentUser: CurrentUser,
        protected router: Router,
        protected toast: Toast,
    ) {
        super(i18n);
    }

    /**
     * Redirect user to login page or show toast informing
     * user that he does not have required permissions.
     */
    protected handle403Error(response: object, errorNum?: number) {
        //if user doesn't have access, navigate to login page        
        // if(errorNum && this.currentUser.isLoggedIn()){                                    
        //     this.currentUser.clear();
        //     try {Android.userLogout(JSON.stringify(this.currentUser.getModel()));}catch(error) {}
        //     this.router.navigate(['/login']);            
        //     let msg = "You are logged on to another device or your session has expired!";
        //     this.toast.open(response['message'] ? response['message'] : msg);
        // }else 
        if (this.currentUser.isLoggedIn()) {
            let msg = "You don't have required permissions to do that.";
            this.toast.open(response['message'] ? response['message'] : msg);
        } else {
            this.router.navigate(['/login']);
        }        
    }
}
