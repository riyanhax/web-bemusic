import {Component, ViewEncapsulation} from "@angular/core";
import {AuthService} from "../auth.service";
import {SocialAuthService} from "../social-auth.service";
import {CurrentUser} from "../current-user";
import {Router} from "@angular/router";
import {Settings} from "../../core/config/settings.service";
import {Toast} from "../../core/ui/toast.service";

declare let Android: any;

@Component({
    selector: 'register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class RegisterComponent {

    isPhone = true;
    isSMS = false;
    savePhone = '';
    countryCode = '+213';
    
    /**
     * Register credentials model.
     */
    public model: {
        email?: string,
        password?: string,
        password_confirmation?: string,
        purchase_code?: string,
        phone?: string,
        sms_code?: string,
    } = {};

    /**
     * Errors returned from backend.
     */
    public errors: {email?: string, password?: string, general?: string, phone?: string, sms_code?: string,} = {};

    /**
     * Whether backend request is in progress currently.
     */
    public isLoading = false;

    /**
     * Whether mobile layout should be activated.
     */
    public isMobile: boolean = false;

    /**
     * RegisterComponent Constructor.
     */
    constructor(
        public auth: AuthService,
        public socialAuth: SocialAuthService,
        public settings: Settings,
        private user: CurrentUser,
        private router: Router,
        private toast: Toast,
    ) {
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    /**
     * Register user and redirect to default authenticated user page.
     */
    public register() {
        this.isLoading = true;

        if(this.isPhone && !this.isSMS){
            if(this.countryCode.slice(-3) == '213' && this.model.email.charAt(0) == '0'){
                this.model.email = this.model.email.substr(1);
            }
            this.savePhone = this.model.email;
            this.model.email = this.countryCode + this.model.email;
        }else{
            this.savePhone = '';
        }

        this.auth.register(this.model).subscribe(response => {
            this.isLoading = false;

            if (this.settings.get('require_email_confirmation')) {
                this.toast.open('We have sent you an email with instructions on how to activate your account.');
            } else if(response['reg'] == 'sms_required'){
                this.isSMS = true;                
            } else {
                this.user.assignCurrent(response.data);
                if(this.isMobile){
                    try {Android.userRegister(JSON.stringify(this.user.getModel()));}catch(error) {}                                        
                }
                this.router.navigate([this.auth.getRedirectUri()]).then(() => {
                    this.toast.open('Registered successfully.');
                });
            }
        }, response => {

            if(this.isPhone){
                this.model.email = this.savePhone;
            }

            if(response['messages']['email'] && response['messages']['email'][0] == 'The email must be 12 digits.')
            {
                response['messages']['email'][0] = 'The phone number must be 12 digits.'
            }
            this.errors = response['messages'];
            
            this.isLoading = false;
        });
    }
}
