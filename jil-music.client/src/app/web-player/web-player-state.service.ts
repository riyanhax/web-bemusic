import {Injectable} from '@angular/core';

import {Settings} from "vebto-client/core/config/settings.service";
import {CurrentUser} from "vebto-client/auth/current-user";
import {Users} from "vebto-client/auth/users.service";

@Injectable()
export class WebPlayerState {

    /**
     * Whether web player is currently loading (globally)
     */
    public loading: boolean = false;

    /**
     * Whether need show ADS modal (globally)
     */
    public ads: boolean = false;

    /**
     * Whether web player track play (globally)
     */
    public countTrackPlay: number = 0;

    /**
     * Whether web player track ads show (globally)
     */
    public trackBeforeAds: number = -1;

    public videoAdsSource: string[] = [];

    public videoAdsLimit: number = -1;

    /**
     * Modal single page
     */

    public isModalOpen: boolean = false;
    public typeModalPage: string = '';
    
    /**
     * Whether mobile layout should be activated.
     */
    public isMobile: boolean = false;

    constructor(
        private settingsAdv: Settings,
        private currUser: CurrentUser,
        private userService: Users,
    ) {
        this.isMobile = window.matchMedia && window.matchMedia('(max-width: 768px)').matches;
        this.countTrackPlay = 0;        
        this.videoAdsSource = this.settingsAdv.has('videoads_source') ? JSON.parse(this.settingsAdv.get('videoads_source')) : [];
        this.trackBeforeAds = this.settingsAdv.get('videoads_count') ? this.settingsAdv.get('videoads_count') : -1;
        if(!this.trackBeforeAds || this.trackBeforeAds <= 0){
                this.trackBeforeAds = -1;
        }  
        this.videoAdsLimit = this.settingsAdv.get('videoads_limit') ? this.settingsAdv.get('videoads_limit') : -1;   
        if(!this.videoAdsLimit || this.videoAdsLimit <= 0){
            this.videoAdsLimit = -1;
    }     
    }

    public adsType(){
        return this.videoAdsSource;
    }

    public adsTypeDelete(type:string){
        let i = this.videoAdsSource.findIndex(index => index === type);
        if (i !== -1) this.videoAdsSource.splice(i, 1);
    }

    /**
     * Need Ads show?
     */
    public adsShow() {
        if (!this.currUser.get('ads_limit') || this.videoAdsLimit > this.currUser.get('ads_limit')) {            
            if(this.trackBeforeAds < 0){                           
                return false;
            }else if(this.countTrackPlay < this.trackBeforeAds){
                this.countTrackPlay++;
                return false;
            }else{            
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Ads showed!
     */
    public adsShowed() {
        this.countTrackPlay = 0;

        if(this.currUser.get('ads_limit')) this.userService.changeAdsLimit(this.currUser.get('id')).subscribe(() => {});
    }

}
