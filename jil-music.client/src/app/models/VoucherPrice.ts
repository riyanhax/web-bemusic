export class VoucherPrice {
	id: number;
	price: number;
		
	constructor(params: Object = {}) {
        for (let name in params) {
            this[name] = params[name];
        }
    }
}