export class UserSubscription {
	action: string;
	amount: number;
	validity: number;
	created_at: string;
		
	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}