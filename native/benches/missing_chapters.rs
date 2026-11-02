use komikita_native::missing_chapters_count;
use std::{collections::HashSet, hint::black_box, time::Instant};
#[allow(clippy::cast_possible_truncation)]
fn main() {
    for size in [100, 1_000, 10_000, 100_000] {
        let numbers: Vec<f64> = (0..size).rev().map(|i| f64::from(i * 2)).collect();
        let rounds = 100;
        let start = Instant::now();
        for _ in 0..rounds {
            black_box(missing_chapters_count(black_box(&numbers)));
        }
        let native = start.elapsed();
        let start = Instant::now();
        for _ in 0..rounds {
            let unique: HashSet<i32> = black_box(&numbers).iter().map(|n| *n as i32).collect();
            let mut values: Vec<i32> = unique.into_iter().collect();
            values.sort_unstable();
            black_box(values);
        }
        println!(
            "chapters={size} iterations={rounds} contiguous={native:?} hash_then_sort={:?}",
            start.elapsed()
        );
    }
}
