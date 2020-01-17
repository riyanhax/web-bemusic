import {Component, ViewEncapsulation} from "@angular/core";
import {SocialAuthService} from "../social-auth.service";
import {AuthService} from "../auth.service";
import {Router} from "@angular/router";
import {CurrentUser} from "../current-user";
import {Bootstrapper} from "../../core/bootstrapper.service";
import {Settings} from "../../core/config/settings.service";

declare let Android: any;

@Component({
    selector: 'login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    encapsulation: ViewEncapsulation.None,
})
export class LoginComponent {

    isPhone = true;    
    savePhone = '';    
    isSMS = false;
    countryCode = '+213';

    /**
     * Login credentials model.
     */
    public model: {email?: string, password?: string, remember?: boolean, phone?: string, sms_code?: string} = {remember: true};

    /**
     * Errors returned from backend.
     */
    public errors: {email?: string, password?: string, general?: string, phone?: string, sms_code?: string} = {};

    /**
     * Whether backend request is in progress currently.
     */
    public isLoading = false;

    /**
     * Whether mobile layout should be activated.
     */
    public isMobile: boolean = false;

    /**
     * LoginComponent Constructor.
     */
    constructor(
        public auth: AuthService,
        public socialAuth: SocialAuthService,
        public settings: Settings,
        private router: Router,
        private user: CurrentUser,
        private bootstrapper: Bootstrapper
    ) {
        this.hydrateModel();
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
    }

    /**
     * Log user in and redirect to default auth user page.
     */
    public login() {
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
        
        this.auth.login(this.model).subscribe(response => {

            if(response['reg'] == 'sms_required'){
                this.isLoading = false;
                this.isSMS = true;                
            }else{
                this.bootstrapper.bootstrap(response.data);
                if(this.isMobile){
                    try {Android.userLogin(JSON.stringify(this.user.getModel()));}catch(error) {}                    
                }
                //TODO: Move this into auth service, so other components can re-use
                this.router.navigate([this.auth.getRedirectUri()]).then(navigated => {
                    this.isLoading = false;

                    if ( ! navigated) {
                        this.router.navigate([this.auth.getRedirectUri()]);
                    }
                })
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

    private hydrateModel() {
        if (this.settings.get('vebto.site.demo')) {
            this.model.email = 'admin@admin.com';
            this.model.password = 'admin';
        }
    }
}
