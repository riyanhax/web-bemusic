export class SubscriptionsType {
	id: number;
	name: string;
	validity: number;
	price: number;
	status: string;	
		
	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}