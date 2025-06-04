
import { Component, OnInit } from '@angular/core';
import { IOrder, Order as OrderService } from '../services/order';
import { CommonModule } from '@angular/common';

export class OrderForm {}
@Component({
  selector: 'app-order-list',
  imports: [CommonModule],
  templateUrl: './order-list.html',
  styleUrl: './order-list.css'
})
export class OrderList implements OnInit {
  orders: IOrder[] = [];

  constructor(private orderService: OrderService) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.orderService.getOrders().subscribe({
      next: (orders) => this.orders = orders,
      error: (err) => console.error('Error loading orders', err)
    });
  }
}
