import {Component, OnInit, OnDestroy, ViewEncapsulation, HostListener, ViewChild, ElementRef} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Settings} from "vebto-client/core/config/settings.service";
import {WebPlayerUrls} from "../../web-player-urls.service";
import {Subscription} from "rxjs";
import {filter} from "rxjs/operators";
import {User} from "vebto-client/core/types/models/User";
import {CurrentUser} from "vebto-client/auth/current-user";
import {Users} from "vebto-client/auth/users.service";
import {AppHttpClient} from "vebto-client/core/http/app-http-client.service";
import {WebPlayerImagesService} from '../../web-player-images.service';

import {WebPlayerState} from "../../web-player-state.service";
import { Location } from '@angular/common';

@Component({
    selector: 'user-profile-page',
    templateUrl: './user-profile-page.component.html',
    styleUrls: ['./user-profile-page.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class UserProfilePageComponent implements OnInit, OnDestroy {
    @ViewChild('scrollContainer') private scrollContainer: ElementRef;
    @ViewChild('scrollImgSmall') private scrollImgSmall: ElementRef;
    // public scrollContainerScrolled:boolean = false;
    public scrollHeaderScrolled:boolean = false;
    @HostListener('scroll', ['$event'])
        onScroll(event) {
            if(this.state.isMobile){
                const scrollPosition = this.scrollContainer.nativeElement.scrollTop;
                
                if(scrollPosition < 150){                                
                    this.scrollImgSmall.nativeElement.style.height = (150-scrollPosition/1.8)+'px';
                    this.scrollImgSmall.nativeElement.style.width = (150-scrollPosition/1.8) +'px';
                }

                if(scrollPosition > 150 && !this.scrollHeaderScrolled){
                    this.scrollHeaderScrolled = true;
                    this.scrollContainer.nativeElement.classList.add('header-static');                                    
                }else if(scrollPosition < 150 && this.scrollHeaderScrolled){
                    this.scrollHeaderScrolled = false;
                    this.scrollContainer.nativeElement.classList.remove('header-static');                    
                }
            }
        }
    /**
     * Active component subscriptions.
     */
    private subscriptions: Subscription[] = [];

    /**
     * User model.
     */
    public user: User = new User({playlists: []});

    /**
     * Currently active tab.
     */
    public activeTab = 'playlists';

    /**
     * UserProfilePageComponent Constructor.
     */
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private settings: Settings,
        public urls: WebPlayerUrls,
        private users: Users,
        public currentUser: CurrentUser,
        private http: AppHttpClient,
        public images: WebPlayerImagesService,

        public state: WebPlayerState,
        private location: Location,
        protected el: ElementRef,
    ) {}

    ngOnInit() {
        this.setActiveTabFromUrl(this.router.url);
        this.bindToRoutingEvents();
    }

    ngOnDestroy() {
        this.subscriptions.forEach(subscription => {
            subscription.unsubscribe();
        });
        this.subscriptions = [];
    }

    goBack() {
        this.location.back(); // go back to previous location
    }

    /**
     * Get profile background image url.
     */
    public getProfileBackground() {
        return this.images.getDefault('artistBig')
    }

    /**
     * Check if active tab matches specified one.
     */
    public activeTabIs(name: string) {
        return this.activeTab === name;
    }

    /**
     * Follow specified user with currently logged in user.
     */
    public follow(user: User) {
        this.http.post('users/'+user.id+'/follow').subscribe(() => {
            this.currentUser.getModel().followed_users.push(user);
        });
    }

    /**
     * Unfollow specified user with currently logged in user.
     */
    public unfollow(user: User) {
        this.http.post('users/'+user.id+'/unfollow').subscribe(() => {
            const followedUsers = this.currentUser.getModel().followed_users,
                i = followedUsers.findIndex(curr => curr.id === user.id);

            followedUsers.splice(i, 1);
        });
    }

    public currentUserIsFollowing(user: User): boolean {
        if ( ! this.currentUser.getModel().followed_users) return false;
        return !!this.currentUser.getModel().followed_users.find(curr => curr.id === user.id);
    }

    /**
     * Check if specified user is currently logged in.
     */
    public isCurrentUser(user: User) {
        return user.id === this.currentUser.get('id');
    }

    /**
     * Bind to router state change events.
     */
    private bindToRoutingEvents() {
        //set user model
        this.route.data.subscribe(data => {
            this.user = data.user;
        });

        //change active tab
        const sub = this.router.events
            .pipe(filter(event => event instanceof NavigationEnd))
            .subscribe((event: NavigationEnd) => {
                this.setActiveTabFromUrl(event.url);
            });

        this.subscriptions.push(sub);
    }

    /**
     * Set currently active tab based on specified url.
     */
    private setActiveTabFromUrl(url: string) {
        const tab = url.split('/').pop();

        switch (tab) {
            case 'following':
                this.activeTab = 'following';
                break;
            case 'followers':
                this.activeTab = 'followers';
                break;
            default:
                this.activeTab = 'playlists';
        }
    }
}
