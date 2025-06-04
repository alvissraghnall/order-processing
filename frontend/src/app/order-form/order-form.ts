import { Component, OnInit, ɵENABLE_ROOT_COMPONENT_BOOTSTRAP } from '@angular/core';
import { Order as OrderService } from '../services/order';
import { Inventory as InventoryService, type IProduct } from '../services/inventory';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-order-form',
  templateUrl: './order-form.html',
  styleUrls: ['./order-form.css'],
  imports: [CommonModule, FormsModule],
})
export class OrderForm implements OnInit {
  products: IProduct[] = [];
  order = {
    customerName: '',
    productId: null,
    quantity: 1
  };

  constructor(
    private orderService: OrderService,
    private inventoryService: InventoryService
  ) { }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.inventoryService.getProducts().subscribe({
      next: (response) => {
        if (response.success) {
          this.products = response.success.products;
        }
      },
      error: (err) => console.error('Error loading products', err)
    });
  }

  onSubmit(): void {
    this.orderService.createOrder(this.order).subscribe({
      next: (order) => {
        alert('Order created successfully!');
        // Reset form
        this.order = {
          customerName: '',
          productId: null,
          quantity: 1
        };
      },
      error: (err) => alert('Error creating order: ' + err.message)
    });
  }
}
