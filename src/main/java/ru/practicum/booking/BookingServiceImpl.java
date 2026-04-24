package ru.practicum.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.item.Item;
import ru.practicum.item.ItemRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(Long userId, BookingCreateDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (dto.getStart().isBefore(LocalDateTime.now())){
            throw new BadRequestException("Укажите дату начала бронирования не позже текущей даты");
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new RuntimeException("Статус бронирования уже изменён");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ForbiddenException("Нет доступа к бронированию");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public  List<BookingDto> getByBooker(Long userId, BookingState state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findAllByBooker(userId, pageable);
            case CURRENT -> bookingRepository.findCurrentByBooker(userId, now, pageable);
            case PAST -> bookingRepository.findPastByBooker(userId, now, pageable);
            case FUTURE -> bookingRepository.findFutureByBooker(userId, now, pageable);
            case WAITING -> bookingRepository.findByStatusBooker(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByStatusBooker(userId, BookingStatus.REJECTED, pageable);
        };

        return page.stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, BookingState state, int from, int size) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findAllByOwner(ownerId, pageable);
            case CURRENT -> bookingRepository.findCurrentByOwner(ownerId, now, pageable);
            case PAST -> bookingRepository.findPastByOwner(ownerId, now, pageable);
            case FUTURE -> bookingRepository.findFutureByOwner(ownerId, now, pageable);
            case WAITING -> bookingRepository.findByStatusOwner(ownerId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByStatusOwner(ownerId, BookingStatus.REJECTED, pageable);
        };

        return page.stream()
                .map(BookingMapper::toDto)
                .toList();
    }
}
