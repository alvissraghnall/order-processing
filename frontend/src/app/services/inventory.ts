import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IProduct {
  id: number;
  name: string;
  price: number;
  stockQuantity: number;
}

export interface IInventory {
  products: IProduct;
}

@Injectable({
  providedIn: 'root'
})
export class Inventory {
  private apiUrl = 'http://localhost:8080/api/orders/products';

  private http = inject(HttpClient);

  getProducts(): Observable<any> {
    return this.http.get<IInventory>(this.apiUrl);
  }
}
