import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IOrder {
  id?: number;
  customerName: string;
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class Order {
  private apiUrl = 'http://localhost:8080/api/orders';

  private http = inject(HttpClient);

  createOrder(order: { customerName: string, productId: number | null, quantity: number }): Observable<IOrder> {
    return this.http.post<IOrder>(this.apiUrl, order);
  }

  getOrders(): Observable<IOrder[]> {
    return this.http.get<IOrder[]>(this.apiUrl);
  }
}
