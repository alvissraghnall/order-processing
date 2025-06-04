import {
  Component,
  inject,
  OnInit,
  ɵENABLE_ROOT_COMPONENT_BOOTSTRAP,
} from '@angular/core';
import { Order as OrderService } from '../services/order';
import {
  Inventory as InventoryService,
  type IProduct,
} from '../services/inventory';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of, catchError, throwError } from 'rxjs';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-order-form',
  templateUrl: './order-form.html',
  styleUrls: ['./order-form.css'],
  imports: [CommonModule, FormsModule, MatSnackBarModule],
})
export class OrderForm implements OnInit {
  products: IProduct[] = [];
  order = {
    customerName: '',
    productId: null,
    quantity: 1,
  };
  private snackBar: MatSnackBar = inject(MatSnackBar);

  constructor(
    private orderService: OrderService,
    private inventoryService: InventoryService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.inventoryService
      .getProducts()
      .pipe(
        catchError(error => {
          console.error('Error loading products', error);
          this.snackBar.open('Failed to load products', 'Close', {
            duration: 3000,
          });
          this.products = [];
          return of({ success: { products: [] } });
        })
      )
      .subscribe(response => {
        if (response.success) {
          this.products = response.success.products;
        }
      });
  }

  onSubmit(): void {
    this.orderService
      .createOrder(this.order)
      .pipe(
        catchError(err => {
          const errorMsg =
            err?.error?.error || 'An unexpected error occurred';

          this.snackBar.open('Error: ' + errorMsg, 'Close', {
            duration: 4000,
            panelClass: ['snackbar-error'],
          });

          return throwError(() => err);
        })
      )
      .subscribe({
        next: order => {
          this.snackBar.open('Order created successfully!', 'Close', {
            duration: 3000,
            panelClass: ['snackbar-success'],
          });

          this.order = {
            customerName: '',
            productId: null,
            quantity: 1,
          };
        },
      });
  }
}
