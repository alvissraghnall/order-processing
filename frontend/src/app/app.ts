import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { OrderForm } from './order-form/order-form';
import { OrderList } from './order-list/order-list';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, OrderForm, OrderList],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected title = 'frontend';
}
