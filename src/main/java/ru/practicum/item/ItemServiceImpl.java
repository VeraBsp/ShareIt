package ru.practicum.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import ru.practicum.booking.BookingStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, CommentRepository commentRepository, BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public ItemDto create(ItemDto dto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toItem(dto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemWithBookingDto getById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemWithBookingDto dto = ItemWithBookingDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(new ArrayList<>())
                .build();
        if (item.getOwner().getId().equals(userId)) {
            setBookings(item, dto);
        }
        List<CommentDto> comments = commentRepository
                .findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(c -> CommentDto.builder()
                        .id(c.getId())
                        .text(c.getText())
                        .authorName(c.getAuthor().getName())
                        .created(c.getCreated())
                        .build()
                )
                .toList();

        dto.setComments(comments);

        return dto;
    }

    @Override
    public List<ItemWithBookingDto> getAllByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);

        return items.stream().map(item -> {
            ItemWithBookingDto dto = ItemWithBookingDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .build();

            setBookings(item, dto);

            return dto;
        }).toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasBooking = bookingRepository
                .existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                        userId,
                        itemId,
                        LocalDateTime.now(),
                        BookingStatus.APPROVED
                );

        if (!hasBooking) {
            throw new BadRequestException("Пользователь не арендовал эту вещь");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentDto.builder()
                .id(saved.getId())
                .text(saved.getText())
                .authorName(saved.getAuthor().getName())
                .created(saved.getCreated())
                .build();
    }

    private void setBookings(Item item, ItemWithBookingDto dto) {
        List<Booking> bookings = bookingRepository
                .findByItem_IdAndStatusOrderByStartDesc(item.getId(), BookingStatus.APPROVED);

        LocalDateTime now = LocalDateTime.now();

        bookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .findFirst()
                .ifPresent(b -> dto.setLastBooking(b.getStart()));

        bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .reduce((first, second) -> second)
                .ifPresent(b -> dto.setNextBooking(b.getStart()));
        dto.setComments(getComments(item.getId()));
    }

    private List<CommentDto> getComments(Long itemId) {
        return commentRepository.findByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(c -> CommentDto.builder()
                        .id(c.getId())
                        .text(c.getText())
                        .authorName(c.getAuthor().getName())
                        .created(c.getCreated())
                        .build())
                .toList();
    }
}
