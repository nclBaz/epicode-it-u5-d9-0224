package riccardogulin.u5d9.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import riccardogulin.u5d9.entities.User;
import riccardogulin.u5d9.exceptions.BadRequestException;
import riccardogulin.u5d9.exceptions.NotFoundException;
import riccardogulin.u5d9.payloads.NewUserDTO;
import riccardogulin.u5d9.repositories.UsersRepository;

import java.io.IOException;
import java.util.UUID;

@Service
public class UsersService {
	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private Cloudinary cloudinaryUploader;

	public Page<User> getUsers(int pageNumber, int pageSize, String sortBy) {
		if (pageSize > 100) pageSize = 100;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy));
		return usersRepository.findAll(pageable);
	}

	public User save(NewUserDTO body) {
		// 1. Verifico se l'email è già in uso
		this.usersRepository.findByEmail(body.email()).ifPresent(
				// 1.1 Se lo è triggero un errore
				user -> {
					throw new BadRequestException("L'email " + body.email() + " è già in uso!");
				}
		);

		// 2. Altrimenti creiamo un nuovo oggetto User e oltre a prendere i valori dal body, aggiungiamo l'avatarURL (ed eventuali altri campi server-generated)
		User newUser = new User(body.name(), body.surname(), body.email(), body.password());

		newUser.setAvatarURL("https://ui-avatars.com/api/?name=" + body.name() + "+" + body.surname());

		// 3. Poi salviamo lo user
		return usersRepository.save(newUser);
	}

	public User findById(UUID userId) {
		return this.usersRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
	}

	public User findByIdAndUpdate(UUID userId, User modifiedUser) {
		User found = this.findById(userId);
		found.setName(modifiedUser.getName());
		found.setSurname(modifiedUser.getSurname());
		found.setEmail(modifiedUser.getEmail());
		found.setPassword(modifiedUser.getPassword());
		found.setAvatarURL("https://ui-avatars.com/api/?name=" + modifiedUser.getName() + "+" + modifiedUser.getSurname());
		return this.usersRepository.save(found);
	}

	public void findByIdAndDelete(UUID userId) {
		User found = this.findById(userId);
		this.usersRepository.delete(found);
	}

	public String uploadImage(MultipartFile file) throws IOException {
		return (String) cloudinaryUploader.uploader().upload(file.getBytes(), ObjectUtils.emptyMap()).get("url");
	}
}
