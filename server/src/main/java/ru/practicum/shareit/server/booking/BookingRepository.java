package ru.practicum.shareit.server.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
           SELECT b FROM Booking b
           WHERE b.booker.id = :userId
           ORDER BY b.start DESC
           """)
    Page<Booking> findAllByBooker(Long userId, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.booker.id = :userId
             AND b.start <= :now
             AND b.end >= :now
           ORDER BY b.start DESC
           """)
    Page<Booking> findCurrentByBooker(Long userId, LocalDateTime now, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.booker.id = :userId
             AND b.end < :now
           ORDER BY b.start DESC
           """)
    Page<Booking> findPastByBooker(Long userId, LocalDateTime now, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.booker.id = :userId
             AND b.start > :now
           ORDER BY b.start DESC
           """)
    Page<Booking> findFutureByBooker(Long userId, LocalDateTime now, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.booker.id = :userId
             AND b.status = :status
           ORDER BY b.start DESC
           """)
    Page<Booking> findByStatusBooker(Long userId, BookingStatus status, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.item.owner.id = :ownerId
           ORDER BY b.start DESC
           """)
    Page<Booking> findAllByOwner(Long ownerId, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.item.owner.id = :ownerId
             AND b.start <= :now
             AND b.end >= :now
           ORDER BY b.start DESC
           """)
    Page<Booking> findCurrentByOwner(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.item.owner.id = :ownerId
             AND b.end < :now
           ORDER BY b.start DESC
           """)
    Page<Booking> findPastByOwner(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.item.owner.id = :ownerId
             AND b.start > :now
           ORDER BY b.start DESC
           """)
    Page<Booking> findFutureByOwner(Long ownerId, LocalDateTime now, Pageable pageable);

    @Query("""
           SELECT b FROM Booking b
           WHERE b.item.owner.id = :ownerId
             AND b.status = :status
           ORDER BY b.start DESC
           """)
    Page<Booking> findByStatusOwner(Long ownerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItem_IdAndStatusOrderByStartDesc(Long itemId, BookingStatus status);

    boolean existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, LocalDateTime time, BookingStatus status);

    Optional<Booking> findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(Long itemId, LocalDateTime now, BookingStatus status);

    Optional<Booking> findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now, BookingStatus status);
}
