import {Social} from "./Social";
import {SocialProfile} from "./SocialProfile";
import {Group} from "./Group";
import {Subscription} from "./Subscription";

export class User {
	id: number;
	username?: string;
	first_name?: string;
	last_name?: string;
    company?: string;
	phone?: string;
	sms_code?: string;
	avatar?: string;
	gender?: string;
	permissions?: string;
	email: string;
	password: string;
	language: string;
	timezone: string;
	country: string;
	remember_token?: string;
	created_at: string = '0000-00-00 00:00:00';
	updated_at: string = '0000-00-00 00:00:00';
	stripe_active: boolean;
	stripe_id?: string;
	stripe_subscription?: string;
	stripe_plan?: string;
	last_four?: string;
	trial_ends_at?: string;
	is_subscribed?: boolean;
	subscriptions?: Subscription[];
	subscription_ends_at?: string;
	confirmed: boolean = true;
	groups: Group[] = [];
	confirmation_code?: string;
	isAdmin: string;
	social_profiles: SocialProfile[];
	has_password: boolean;
	oauth?: Social[];
	
	subscription: string = '0000-00-00 00:00:00';
	subscription_s: boolean;

	ads_limit?: number;
	ads_limit_date?: string = '0000-00-00';

	session_id?: string;

	//TEMP
	followed_users?: any[];
	followers?: any[];

	followers_count?: number;

	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}