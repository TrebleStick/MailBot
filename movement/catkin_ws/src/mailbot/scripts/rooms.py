import numpy as np

room_list = np.asarray([508, 507, 510])

#array of rroms to arax of index
def arrayToIndex(array) :
    indexBois = array
    for i in range(len(array)) :
        try:
            indexBois[i] = np.argwhere(room_list == array[i])[0][0]

        except IndexError :
            print('Room ', array[i], ' not found.')
            indexBois[i] = -1

    return indexBois

#individial index to room
def indexToRoom(index) :
    try:
        return room_list[index]
    except IndexError :
        print('Index ', index, ' out of range, try between 0 to ', len(room_list)-1)
        return -1
    except NameError  :
        print('Index ', index, ' out of range, try between 0 to ', len(room_list)-1)
        return -1

#individual room to index
def roomToIndex(room) :
    try:
        return np.argwhere(room_list == room)[0][0]

    except IndexError :
        print('Room ', room, ' not found.')
        return -1
