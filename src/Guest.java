record Guest(String name, String surname) {
    Guest {
        if (name == null || surname == null || name.isBlank() || surname.isBlank()) {
            throw new IllegalArgumentException("Guest name and surname must not be blank.");
        }

        name = name.strip();
        surname = surname.strip();
    }

    public String fullName() {
        return name + " " + surname;
    }
}
